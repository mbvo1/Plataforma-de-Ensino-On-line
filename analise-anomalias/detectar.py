"""Fase 5: deteccao de anomalias no log de seguranca do SIGEA.

    # contra o app (baixa eventos e rotulos pela API; --enviar devolve as anomalias)
    python detectar.py --sigea http://localhost:8080 --enviar

    # sobre CSVs ja baixados
    python detectar.py --eventos dados/log-seguranca.csv --rotulos dados/rotulos.csv

Grava em sigea/evidencias/projeto-2/:
    deteccao.csv            metricas dos dois modelos contra a verdade conhecida
    deteccao-features.csv   media de cada feature por grupo (normal e cada cenario)
    deteccao-zscore-feature-dominante.csv  qual feature disparou o z-score, em acertos e falsos positivos
    deteccao-isolation-forest-arvores.csv  em quantas arvores cada feature variou e foi usada em corte
    deteccao-entrada.csv    tamanho e SHA-256 da entrada, para amarrar o resultado ao volume
"""

from __future__ import annotations

import argparse
import csv
import hashlib
import sys
from pathlib import Path

import pandas as pd

import avaliacao
import features
import modelos
from sigea_api import ClienteSigea

AQUI = Path(__file__).resolve().parent
EVIDENCIAS = AQUI.parent / "sigea" / "evidencias" / "projeto-2"
DADOS = AQUI / "dados"


def sha256(caminho: Path) -> str:
    h = hashlib.sha256()
    with open(caminho, "rb") as f:
        for bloco in iter(lambda: f.read(1 << 20), b""):
            h.update(bloco)
    return h.hexdigest()


# Trocado por --saida. Na demonstracao ao vivo o log tem eventos reais a mais
# (logins, cifragens, esteganografia), as janelas mudam, e gravar por cima
# das evidencias trocaria os numeros do relatorio pelos de uma execucao
# que ninguem versionou.
saida = EVIDENCIAS


def gravar_csv(nome: str, cabecalho: list[str], linhas: list[list]) -> None:
    saida.mkdir(parents=True, exist_ok=True)
    destino = saida / nome
    with open(destino, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(cabecalho)
        w.writerows(linhas)
    print(f"[Evidencias] {destino} ({len(linhas)} linhas)")


def itens_para_envio(janelas: pd.DataFrame, d: modelos.Deteccao) -> list[dict]:
    """Corpo do POST /api/admin/anomalias: uma janela marcada por item, horario em UTC sem fuso."""
    itens = []
    for (_, j), score in zip(janelas[d.marcada].iterrows(), d.score[d.marcada]):
        usuario = int(j["entidade"][2:]) if j["entidade"].startswith("u:") else None
        itens.append({
            "usuarioId": usuario,
            "inicioJanela": j["inicio"].tz_convert("UTC").strftime("%Y-%m-%dT%H:%M:%S"),
            "fimJanela": j["fim"].tz_convert("UTC").strftime("%Y-%m-%dT%H:%M:%S"),
            "score": round(float(score), 6),
            "modelo": d.modelo,
        })
    return itens


def main(argv: list[str]) -> int:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--sigea", help="URL base do SIGEA, ex.: http://localhost:8080")
    p.add_argument("--eventos", type=Path, help="CSV de /api/admin/logs/export (com --sigea: nao exporta, so envia)")
    p.add_argument("--rotulos", type=Path, help="CSV de /api/admin/experimento/rotulos (com --sigea: nao exporta, so envia)")
    p.add_argument("--enviar", action="store_true", help="devolve as janelas marcadas via POST /api/admin/anomalias")
    p.add_argument("--saida", type=Path, help=f"pasta dos CSVs de resultado (padrao: {EVIDENCIAS})")
    a = p.parse_args(argv)
    if a.saida:
        global saida
        saida = a.saida

    cliente = None
    if a.sigea and a.eventos and a.rotulos:
        # Entrada congelada (a mesma do relatorio) e envio ao app: nada e
        # exportado. Retreinar sobre o log ao vivo incluiria os eventos da
        # propria demonstracao, com o horario dela, e deslocaria a fronteira
        # do IsolationForest; com a entrada fixa, o resultado e o versionado.
        cliente = ClienteSigea(a.sigea)
    elif a.sigea:
        cliente = ClienteSigea(a.sigea)
        a.eventos = cliente.exportar_eventos(DADOS / "log-seguranca.csv")
        a.rotulos = cliente.exportar_rotulos(DADOS / "rotulos.csv")
    elif not (a.eventos and a.rotulos):
        p.error("informe --sigea ou --eventos e --rotulos")
    if a.enviar and cliente is None:
        p.error("--enviar exige --sigea")

    eventos = features.carregar_eventos(a.eventos)
    rotulos = features.carregar_rotulos(a.rotulos)
    janelas = features.rotular(features.agregar(eventos), rotulos)
    cenarios = sorted(rotulos["cenario"].unique())
    cenario_do_incidente = dict(zip(rotulos["incidente_id"].astype(int), rotulos["cenario"]))
    ignorados = int((eventos["tipo_evento"] == "ANOMALIA_DETECTADA").sum())
    print(f"{len(eventos)} eventos ({ignorados} ANOMALIA_DETECTADA ignorados), {len(janelas)} janelas, "
          f"{int(janelas['anomala'].sum())} anomalas")

    X = janelas[features.FEATURES_MODELO]
    deteccoes = [modelos.isolation_forest(X), modelos.zscore(X)]

    linhas = []
    for d in deteccoes:
        for m in avaliacao.avaliar(janelas, d, cenarios, cenario_do_incidente):
            linhas.append([avaliacao.formatar(m.get(c)) for c in avaliacao.COLUNAS])
    gravar_csv("deteccao.csv", avaliacao.COLUNAS, linhas)

    grupos = [("normal", ~janelas["anomala"])] + [(c, janelas["cenario"] == c) for c in cenarios]
    cab = ["feature", "categoria_pdf"] + [f"media_{g}" for g, _ in grupos]
    feat = [[f, features.CATEGORIA_DA_FEATURE[f]] + [avaliacao.formatar(float(janelas.loc[m, f].mean())) for _, m in grupos]
            for f in features.FEATURES_MODELO]
    feat.append(["janelas", ""] + [str(int(m.sum())) for _, m in grupos])
    gravar_csv("deteccao-features.csv", cab, feat)

    # De onde vem o falso positivo do z-score: qual feature passou de 3
    # desvios na janela normal marcada. Analise, nao ajuste - o limiar fica.
    z = next(d for d in deteccoes if d.feature_dominante is not None)
    fp = z.marcada & ~janelas["anomala"].to_numpy()
    vp = z.marcada & janelas["anomala"].to_numpy()
    contagem = [[f, int((z.feature_dominante[fp] == f).sum()), int((z.feature_dominante[vp] == f).sum())]
                for f in features.FEATURES_MODELO]
    gravar_csv("deteccao-zscore-feature-dominante.csv",
               ["feature", "falsos_positivos", "verdadeiros_positivos"], contagem)

    gravar_csv("deteccao-isolation-forest-arvores.csv",
               ["feature", "arvores", "arvores_em_que_a_feature_variou_no_treino", "arvores_com_corte_na_feature"],
               [list(t) for t in modelos.diagnostico_arvores(X)])

    gravar_csv("deteccao-entrada.csv", ["arquivo", "linhas_de_dados", "sha256"], [
        ["log-seguranca.csv", len(eventos), sha256(a.eventos)],
        ["rotulos.csv", len(rotulos), sha256(a.rotulos)],
    ])

    for d in deteccoes:
        r = next(m for m in avaliacao.avaliar(janelas, d, cenarios, cenario_do_incidente) if m["escopo"] == "todas")
        print(f"{d.modelo:17s} marcadas={int(d.marcada.sum()):6d} precisao={r['precisao']:.4f} "
              f"recall={r['recall']:.4f} f1={r['f1']:.4f} roc_auc={r['roc_auc']:.4f}")

    if a.enviar:
        for d in deteccoes:
            itens = itens_para_envio(janelas, d)
            respostas = cliente.enviar_anomalias(itens) if itens else []
            registrados = sum(r["registrados"] for r in respostas)
            print(f"enviado {d.modelo}: {registrados} ANOMALIA_DETECTADA em {len(respostas)} requisicao(oes)")
        print("integridade depois do envio:", cliente.verificar_integridade()["status"])
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
