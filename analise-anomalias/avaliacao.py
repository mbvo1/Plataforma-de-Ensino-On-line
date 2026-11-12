"""Metricas contra a verdade conhecida do seeder (Fase 5).

Tres niveis, porque cada um responde uma pergunta diferente:
- por janela, todas juntas: precisao, recall, F1, matriz de confusao, e as
  duas medidas que nao dependem do limiar (ROC-AUC e precisao media), que
  mostram se o ranking do modelo presta mesmo quando o corte escolhido nao;
- por janela, cenario a cenario: so recall. Falso positivo nao pertence a
  cenario nenhum, entao precisao por cenario nao existe;
- por incidente: detectado se pelo menos uma janela dele foi marcada. E a
  pergunta do operador ("fiquei sabendo do ataque?").
"""

from __future__ import annotations

import numpy as np
import pandas as pd
from sklearn.metrics import average_precision_score, roc_auc_score

from modelos import Deteccao

COLUNAS = ["modelo", "escopo", "unidade", "total", "positivos_reais", "vp", "fp", "fn", "vn",
           "precisao", "recall", "f1", "taxa_falsos_positivos", "roc_auc", "precisao_media"]


def _div(a: float, b: float) -> float:
    return a / b if b else float("nan")


def avaliar(janelas: pd.DataFrame, d: Deteccao, cenarios: list[str],
            cenario_do_incidente: dict[int, str]) -> list[dict]:
    real = janelas["anomala"].to_numpy(dtype=bool)
    marcada = d.marcada
    vp = int(np.sum(real & marcada))
    fp = int(np.sum(~real & marcada))
    fn = int(np.sum(real & ~marcada))
    vn = int(np.sum(~real & ~marcada))
    precisao = _div(vp, vp + fp)
    recall = _div(vp, vp + fn)
    linhas = [dict(modelo=d.modelo, escopo="todas", unidade="janela", total=len(real), positivos_reais=int(real.sum()),
                   vp=vp, fp=fp, fn=fn, vn=vn, precisao=precisao, recall=recall,
                   f1=_div(2 * precisao * recall, precisao + recall), taxa_falsos_positivos=_div(fp, fp + vn),
                   roc_auc=roc_auc_score(real, d.score), precisao_media=average_precision_score(real, d.score))]

    for c in cenarios:
        do_cenario = (janelas["cenario"] == c).to_numpy()
        acertos = int(np.sum(do_cenario & marcada))
        linhas.append(dict(modelo=d.modelo, escopo=c, unidade="janela", total=int(do_cenario.sum()),
                           positivos_reais=int(do_cenario.sum()), vp=acertos, fn=int(do_cenario.sum()) - acertos,
                           recall=_div(acertos, do_cenario.sum())))

    # O cenario de cada incidente vem dos rotulos, nao da janela: uma janela
    # pode juntar eventos de dois incidentes do mesmo usuario.
    detectado: dict[int, bool] = {}
    for incs, m in zip(janelas["incidentes"], marcada):
        for i in incs:
            detectado[i] = detectado.get(i, False) or bool(m)
    for c in cenarios + ["todos"]:
        ids = [i for i in detectado if c == "todos" or cenario_do_incidente[i] == c]
        acertos = sum(detectado[i] for i in ids)
        linhas.append(dict(modelo=d.modelo, escopo=c, unidade="incidente", total=len(ids), positivos_reais=len(ids),
                           vp=acertos, fn=len(ids) - acertos, recall=_div(acertos, len(ids))))
    return linhas


def formatar(valor) -> str:
    if valor is None:
        return ""
    if isinstance(valor, float):
        return "" if np.isnan(valor) else f"{valor:.4f}"
    return str(valor)
