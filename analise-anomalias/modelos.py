"""Os dois detectores comparados na Fase 5.

Nenhum dos dois ve o rotulo, e nenhum parametro foi escolhido olhando o
resultado. Os dois limiares abaixo foram fixados antes da primeira execucao:

- IsolationForest com os padroes do scikit-learn (100 arvores, 256 amostras
  por arvore, contamination='auto', que corta no limiar do artigo original
  de Liu et al.). Usar a taxa real de anomalias do seeder como
  contamination seria entregar o rotulo ao modelo.
- z-score: janela anomala se alguma feature passar de |z| > 3, a regra
  classica de "tres desvios". Media e desvio calculados sobre todas as
  janelas, inclusive as anomalas, como faria quem nao tem rotulo.

O z-score esta aqui como linha de base de proposito: se o modelo simples
empatar com o IsolationForest, isso e resultado, nao fracasso.
"""

from __future__ import annotations

from dataclasses import dataclass

import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest

SEMENTE = 20261005
LIMIAR_Z = 3.0


@dataclass(frozen=True)
class Deteccao:
    modelo: str
    score: np.ndarray   # maior = mais anomalo
    marcada: np.ndarray  # bool
    # So no z-score: a feature de maior |z| em cada janela, para explicar
    # os falsos positivos. O IsolationForest nao tem equivalente direto.
    feature_dominante: np.ndarray | None = None


def isolation_forest(X: pd.DataFrame, semente: int = SEMENTE) -> Deteccao:
    # Sem padronizar: a arvore escolhe o corte uniformemente no intervalo de
    # cada feature, entao a escala nao muda o resultado.
    modelo = IsolationForest(random_state=semente)
    modelo.fit(X.to_numpy(dtype=float))
    dados = X.to_numpy(dtype=float)
    return Deteccao("isolation_forest", -modelo.score_samples(dados), modelo.predict(dados) == -1)


def diagnostico_arvores(X: pd.DataFrame, semente: int = SEMENTE) -> list[tuple[str, int, int, int]]:
    """Por feature: em quantas arvores ela variou no treino e em quantas houve corte nela.

    Mesmo modelo de isolation_forest (mesma semente). Serve para explicar
    um cenario que o modelo perde: cada arvore treina com 256 janelas
    sorteadas, e uma feature que so e diferente de zero em poucas janelas
    fica constante na maioria das arvores - e arvore nao corta em feature
    constante.
    """
    dados = X.to_numpy(dtype=float)
    modelo = IsolationForest(random_state=semente).fit(dados)
    saida = []
    for i, nome in enumerate(X.columns):
        variou = sum(int(np.ptp(dados[amostra, i]) > 0) for amostra in modelo.estimators_samples_)
        cortes = sum(int((arvore.tree_.feature == i).any()) for arvore in modelo.estimators_)
        saida.append((nome, len(modelo.estimators_), variou, cortes))
    return saida


def zscore(X: pd.DataFrame, limiar: float = LIMIAR_Z) -> Deteccao:
    dados = X.to_numpy(dtype=float)
    media = dados.mean(axis=0)
    desvio = dados.std(axis=0)
    # Feature constante nao tem z; fica fora em vez de dividir por zero.
    usadas = desvio > 0
    z = np.abs((dados[:, usadas] - media[usadas]) / desvio[usadas])
    if not z.shape[1]:
        return Deteccao("zscore", np.zeros(len(dados)), np.zeros(len(dados), dtype=bool))
    score = z.max(axis=1)
    nomes = np.array(X.columns)[usadas]
    return Deteccao("zscore", score, score > limiar, nomes[z.argmax(axis=1)])
