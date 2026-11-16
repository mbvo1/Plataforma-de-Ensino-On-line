"""Cliente minimo dos endpoints de admin do SIGEA usados na Fase 5.

O Python nao le o .mv.db do H2 direto: o arquivo fica travado pelo app em
execucao e o formato e interno do H2. Tudo passa pela API, com o mesmo
controle de acesso (ADMINISTRADOR) que qualquer outro cliente.

As credenciais vem de variaveis de ambiente (SIGEA_ADMIN_EMAIL,
SIGEA_ADMIN_SENHA) ou do prompt; nunca de argumento de linha de comando,
que fica no historico do shell e na lista de processos. O token nao e
impresso nem gravado.
"""

from __future__ import annotations

import getpass
import os
from pathlib import Path

import requests

LIMITE_POR_REQUISICAO = 10_000  # mesmo limite do AuditoriaSegurancaController
TEMPO_LIMITE_S = 300


class ClienteSigea:
    def __init__(self, base: str, email: str | None = None, senha: str | None = None):
        self.base = base.rstrip("/")
        email = email or os.environ.get("SIGEA_ADMIN_EMAIL") or input("E-mail do administrador: ")
        senha = senha or os.environ.get("SIGEA_ADMIN_SENHA") or getpass.getpass("Senha do administrador: ")
        r = requests.post(f"{self.base}/api/auth/login-admin", json={"email": email, "senha": senha},
                          timeout=TEMPO_LIMITE_S)
        if r.status_code != 200:
            raise RuntimeError(f"login de administrador recusado: HTTP {r.status_code}")
        self._sessao = requests.Session()
        self._sessao.headers["Authorization"] = "Bearer " + r.json()["token"]

    def _baixar(self, caminho: str, destino: Path, parametros: dict) -> Path:
        destino.parent.mkdir(parents=True, exist_ok=True)
        with self._sessao.get(f"{self.base}{caminho}", params=parametros, stream=True, timeout=TEMPO_LIMITE_S) as r:
            r.raise_for_status()
            with open(destino, "wb") as f:
                for bloco in r.iter_content(chunk_size=1 << 16):
                    f.write(bloco)
        return destino

    def exportar_eventos(self, destino: Path, desde: str | None = None, ate: str | None = None) -> Path:
        parametros = {"formato": "csv"}
        if desde:
            parametros["desde"] = desde
        if ate:
            parametros["ate"] = ate
        return self._baixar("/api/admin/logs/export", destino, parametros)

    def exportar_rotulos(self, destino: Path) -> Path:
        return self._baixar("/api/admin/experimento/rotulos", destino, {"formato": "csv"})

    def verificar_integridade(self) -> dict:
        r = self._sessao.get(f"{self.base}/api/admin/auditoria/verificar-integridade", timeout=TEMPO_LIMITE_S)
        r.raise_for_status()
        return r.json()

    def enviar_anomalias(self, itens: list[dict]) -> list[dict]:
        respostas = []
        for i in range(0, len(itens), LIMITE_POR_REQUISICAO):
            r = self._sessao.post(f"{self.base}/api/admin/anomalias", json=itens[i:i + LIMITE_POR_REQUISICAO],
                                  timeout=TEMPO_LIMITE_S)
            if r.status_code != 201:
                raise RuntimeError(f"POST /api/admin/anomalias recusado: HTTP {r.status_code} {r.text[:300]}")
            respostas.append(r.json())
        return respostas
