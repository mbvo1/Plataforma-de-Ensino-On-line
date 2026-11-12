"""Agregacao do log de seguranca em janelas de 5 minutos por usuario (Fase 5).

Por que janela fixa de 5 minutos, e nao janela deslizante: e o que o brief
pede, e cada janela vira exatamente um item que pode ser devolvido ao SIGEA
como ANOMALIA_DETECTADA (inicio, fim, usuario). O preco e conhecido: um
incidente que cruza a fronteira de duas janelas aparece partido nas duas.

Eventos sem usuario_id (login com e-mail que nao existe) sao agrupados pelo
IP. Descarta-los esconderia uma forca bruta contra e-mails inexistentes.

ANOMALIA_DETECTADA nao entra: e a saida do proprio detector. Se entrasse,
rodar o script de novo depois de enviar os resultados mudaria a entrada.
"""

from __future__ import annotations

import math

import numpy as np
import pandas as pd

JANELA = "5min"
DURACAO_JANELA_S = 300.0
# Recife, UTC-3, sem horario de verao desde 2019. O log guarda UTC.
DESLOCAMENTO_LOCAL = pd.Timedelta(hours=-3)

# Feature -> anomalia do enunciado que ela cobre (criterio de aceite da fase).
CATEGORIA_DA_FEATURE = {
    "n_tentativas_login": "Numero incomum de tentativas de autenticacao",
    "n_falhas_login": "Numero incomum de tentativas de autenticacao",
    "taxa_falha": "Numero incomum de tentativas de autenticacao",
    "n_falhas_decifragem": "Frequencia elevada de falhas de descriptografia",
    "n_violacoes_integridade": "Alteracoes inesperadas em arquivos",
    "n_recursos_distintos": "Padroes atipicos de acesso",
    "raridade_hora": "Padroes atipicos de acesso",
    "n_eventos": "Comportamentos incomuns na geracao de eventos",
    "intervalo_medio_entre_eventos": "Comportamentos incomuns na geracao de eventos",
    "desvio_intervalo": "Comportamentos incomuns na geracao de eventos",
}

# O que os modelos recebem. hora_do_dia fica na tabela, mas o modelo ve
# raridade_hora: hora e circular (23h encosta em 0h) e a distribuicao e
# bimodal (manha e tarde), entao nem a hora crua nem o z-score dela dizem
# "horario incomum". raridade_hora = -ln(fracao das janelas naquela hora),
# calculada sobre os proprios dados, sem rotulo.
FEATURES_MODELO = list(CATEGORIA_DA_FEATURE)


def carregar_eventos(origem) -> pd.DataFrame:
    """CSV de GET /api/admin/logs/export?formato=csv."""
    ev = pd.read_csv(origem, dtype={"usuario_id": "Int64", "recurso": "string", "ip": "string"},
                     keep_default_na=False, na_values={"usuario_id": [""]})
    ev["timestamp"] = pd.to_datetime(ev["timestamp"], utc=True, format="ISO8601")
    return ev


def carregar_rotulos(origem) -> pd.DataFrame:
    """CSV de GET /api/admin/experimento/rotulos?formato=csv."""
    return pd.read_csv(origem, usecols=["log_id", "incidente_id", "cenario"])


def entidade(ev: pd.DataFrame) -> pd.Series:
    por_usuario = "u:" + ev["usuario_id"].astype("string")
    por_ip = "ip:" + ev["ip"].astype("string")
    return por_usuario.where(ev["usuario_id"].notna(), por_ip)


def agregar(eventos: pd.DataFrame) -> pd.DataFrame:
    """Uma linha por (entidade, inicio da janela), com as features e a lista de log_id."""
    ev = eventos[eventos["tipo_evento"] != "ANOMALIA_DETECTADA"].copy()
    ev["entidade"] = entidade(ev)
    ev["inicio"] = ev["timestamp"].dt.floor(JANELA)
    ev = ev.sort_values(["entidade", "timestamp", "log_id"], kind="mergesort")

    tipo = ev["tipo_evento"]
    ev["e_login"] = tipo.isin(["LOGIN_SUCESSO", "LOGIN_FALHA"]).astype(int)
    ev["e_falha_login"] = (tipo == "LOGIN_FALHA").astype(int)
    ev["e_falha_decifragem"] = (tipo == "DECIFRAGEM_FALHA").astype(int)
    ev["e_violacao"] = (tipo == "INTEGRIDADE_VIOLADA").astype(int)
    chave = ["entidade", "inicio"]
    ev["intervalo"] = ev.groupby(chave, sort=False)["timestamp"].diff().dt.total_seconds()

    g = ev.groupby(chave, sort=True)
    janelas = pd.DataFrame({
        "n_eventos": g.size(),
        "n_tentativas_login": g["e_login"].sum(),
        "n_falhas_login": g["e_falha_login"].sum(),
        "n_falhas_decifragem": g["e_falha_decifragem"].sum(),
        "n_violacoes_integridade": g["e_violacao"].sum(),
        "n_recursos_distintos": g["recurso"].nunique(),
        "intervalo_medio_entre_eventos": g["intervalo"].mean(),
        # ddof=0: desvio dos intervalos observados, nao estimativa amostral.
        "desvio_intervalo": g["intervalo"].std(ddof=0),
        "n_intervalos": g["intervalo"].count(),
        "log_ids": g["log_id"].agg(list),
    }).reset_index()

    janelas["taxa_falha"] = np.where(janelas["n_tentativas_login"] > 0,
                                     janelas["n_falhas_login"] / janelas["n_tentativas_login"].clip(lower=1), 0.0)
    janelas["hora_do_dia"] = (janelas["inicio"] + DESLOCAMENTO_LOCAL).dt.hour

    # Janela com um evento so nao tem intervalo: o intervalo medio vira a
    # duracao da janela ("nada se repetiu em 5 minutos"). O desvio precisa
    # de pelo menos 2 intervalos; com menos, recebe a mediana das janelas
    # que tem - o valor mais neutro possivel. Sem isso, toda janela com 2
    # eventos teria desvio 0 e pareceria um robo de ritmo perfeito.
    janelas["intervalo_medio_entre_eventos"] = janelas["intervalo_medio_entre_eventos"].fillna(DURACAO_JANELA_S)
    tem_desvio = janelas["n_intervalos"] >= 2
    mediana = janelas.loc[tem_desvio, "desvio_intervalo"].median()
    if not tem_desvio.any() or (isinstance(mediana, float) and math.isnan(mediana)):
        mediana = 0.0
    janelas["desvio_intervalo"] = janelas["desvio_intervalo"].where(tem_desvio, mediana)

    frequencia_hora = janelas["hora_do_dia"].map(janelas["hora_do_dia"].value_counts(normalize=True))
    janelas["raridade_hora"] = -np.log(frequencia_hora)
    janelas["fim"] = janelas["inicio"] + pd.Timedelta(JANELA)
    return janelas


def rotular(janelas: pd.DataFrame, rotulos: pd.DataFrame) -> pd.DataFrame:
    """Acrescenta a verdade conhecida. Janela anomala = contem ao menos um evento rotulado.

    Rodado depois de agregar e fora de FEATURES_MODELO: o rotulo nunca chega
    ao modelo, so a avaliacao.
    """
    cenario_do_log = dict(zip(rotulos["log_id"], rotulos["cenario"]))
    incidente_do_log = dict(zip(rotulos["log_id"], rotulos["incidente_id"]))
    cenarios, incidentes = [], []
    for ids in janelas["log_ids"]:
        cs = [cenario_do_log[i] for i in ids if i in cenario_do_log]
        cenarios.append(max(sorted(set(cs)), key=cs.count) if cs else None)
        incidentes.append(sorted({int(incidente_do_log[i]) for i in ids if i in incidente_do_log}))
    saida = janelas.copy()
    saida["cenario"] = cenarios
    saida["incidentes"] = incidentes
    saida["anomala"] = saida["cenario"].notna()
    return saida
