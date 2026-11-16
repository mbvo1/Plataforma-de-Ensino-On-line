"""Testes da agregacao e das metricas com eventos montados a mao.

    python -m unittest discover -s tests
"""

import io
import math
import sys
import unittest
from pathlib import Path

import numpy as np
import pandas as pd

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import avaliacao  # noqa: E402
import features  # noqa: E402
import modelos  # noqa: E402

CABECALHO = "log_id,timestamp,tipo_evento,usuario_id,recurso,ip,resultado,detalhe,hash_anterior,hash_atual\n"


def eventos(*linhas):
    corpo = "".join(f"{i + 1},{t},{tipo},{u},{rec},{ip},R,\"{{\"\"a\"\":1}}\",h,h\n"
                    for i, (t, tipo, u, rec, ip) in enumerate(linhas))
    return features.carregar_eventos(io.StringIO(CABECALHO + corpo))


class AgregacaoTest(unittest.TestCase):

    def test_janelas_de_5_minutos_por_usuario_e_contagens(self):
        ev = eventos(
            ("2026-10-05T13:00:10.000Z", "LOGIN_FALHA", "7", "/api/auth/login", "192.0.2.1"),
            ("2026-10-05T13:00:20.000Z", "LOGIN_FALHA", "7", "/api/auth/login", "192.0.2.1"),
            ("2026-10-05T13:00:50.000Z", "LOGIN_SUCESSO", "7", "/api/auth/login", "192.0.2.1"),
            ("2026-10-05T13:04:59.999Z", "DECIFRAGEM_FALHA", "7", "/api/avisos/1", "192.0.2.1"),
            ("2026-10-05T13:05:00.000Z", "DECIFRAGEM_OK", "7", "/api/avisos/2", "192.0.2.1"),  # proxima janela
            ("2026-10-05T13:01:00.000Z", "INTEGRIDADE_VIOLADA", "8", "/uploads/x.pdf", "192.0.2.2"),
            ("2026-10-05T13:02:00.000Z", "ANOMALIA_DETECTADA", "7", "janela", "192.0.2.3"),  # ignorado
        )
        j = features.agregar(ev).set_index(["entidade", "inicio"])
        primeira = j.loc[("u:7", pd.Timestamp("2026-10-05T13:00:00Z"))]
        self.assertEqual(primeira["n_eventos"], 4)
        self.assertEqual(primeira["n_tentativas_login"], 3)
        self.assertEqual(primeira["n_falhas_login"], 2)
        self.assertAlmostEqual(primeira["taxa_falha"], 2 / 3)
        self.assertEqual(primeira["n_falhas_decifragem"], 1)
        self.assertEqual(primeira["n_recursos_distintos"], 2)
        self.assertEqual(primeira["hora_do_dia"], 10, "13h UTC = 10h em Recife")
        intervalos = [10.0, 30.0, 249.999]
        self.assertAlmostEqual(primeira["intervalo_medio_entre_eventos"], np.mean(intervalos))
        self.assertAlmostEqual(primeira["desvio_intervalo"], np.std(intervalos))
        self.assertEqual(j.loc[("u:7", pd.Timestamp("2026-10-05T13:05:00Z")), "n_eventos"], 1)
        self.assertEqual(j.loc[("u:8", pd.Timestamp("2026-10-05T13:00:00Z")), "n_violacoes_integridade"], 1)
        self.assertEqual(len(j), 3, "ANOMALIA_DETECTADA nao pode gerar janela")

    def test_evento_sem_usuario_agrupa_por_ip(self):
        ev = eventos(("2026-10-05T13:00:10.000Z", "LOGIN_FALHA", "", "/api/auth/login", "203.0.113.9"),
                     ("2026-10-05T13:00:11.000Z", "LOGIN_FALHA", "", "/api/auth/login", "203.0.113.9"))
        j = features.agregar(ev)
        self.assertEqual(list(j["entidade"]), ["ip:203.0.113.9"])
        self.assertEqual(j["n_falhas_login"].iloc[0], 2)

    def test_janela_curta_recebe_valores_neutros_de_intervalo(self):
        ev = eventos(
            ("2026-10-05T13:00:00.000Z", "DECIFRAGEM_OK", "1", "/a", "192.0.2.1"),  # 1 evento
            ("2026-10-05T14:00:00.000Z", "DECIFRAGEM_OK", "2", "/a", "192.0.2.1"),  # 2 eventos, 1 intervalo
            ("2026-10-05T14:00:30.000Z", "DECIFRAGEM_OK", "2", "/b", "192.0.2.1"),
            ("2026-10-05T15:00:00.000Z", "DECIFRAGEM_OK", "3", "/a", "192.0.2.1"),  # 3 eventos, desvio real
            ("2026-10-05T15:00:10.000Z", "DECIFRAGEM_OK", "3", "/a", "192.0.2.1"),
            ("2026-10-05T15:00:40.000Z", "DECIFRAGEM_OK", "3", "/a", "192.0.2.1"),
        )
        j = features.agregar(ev).set_index("entidade")
        self.assertEqual(j.loc["u:1", "intervalo_medio_entre_eventos"], features.DURACAO_JANELA_S)
        self.assertAlmostEqual(j.loc["u:3", "desvio_intervalo"], 10.0)
        self.assertAlmostEqual(j.loc["u:2", "desvio_intervalo"], 10.0, msg="sem 2 intervalos: mediana das que tem")
        self.assertAlmostEqual(j.loc["u:1", "desvio_intervalo"], 10.0)

    def test_raridade_da_hora(self):
        ev = eventos(*[(f"2026-10-05T13:0{i}:00.000Z", "DECIFRAGEM_OK", str(i), "/a", "192.0.2.1") for i in range(3)],
                     ("2026-10-06T06:00:00.000Z", "DECIFRAGEM_OK", "9", "/a", "192.0.2.1"))
        j = features.agregar(ev).set_index("entidade")
        self.assertAlmostEqual(j.loc["u:0", "raridade_hora"], -math.log(3 / 4))
        self.assertAlmostEqual(j.loc["u:9", "raridade_hora"], -math.log(1 / 4))

    def test_rotulo_marca_a_janela_que_contem_evento_rotulado(self):
        ev = eventos(("2026-10-05T13:00:10.000Z", "LOGIN_FALHA", "7", "/l", "192.0.2.1"),
                     ("2026-10-05T13:00:20.000Z", "LOGIN_SUCESSO", "7", "/l", "192.0.2.1"),
                     ("2026-10-05T13:10:00.000Z", "LOGIN_SUCESSO", "7", "/l", "192.0.2.1"))
        rot = pd.DataFrame({"log_id": [2], "incidente_id": [5], "cenario": ["FORCA_BRUTA_LOGIN"]})
        j = features.rotular(features.agregar(ev), rot)
        self.assertEqual(list(j["anomala"]), [True, False])
        self.assertEqual(j["incidentes"].iloc[0], [5])
        self.assertNotIn("cenario", features.FEATURES_MODELO)
        self.assertNotIn("anomala", features.FEATURES_MODELO)

    def test_toda_categoria_do_enunciado_tem_feature(self):
        self.assertEqual(len(set(features.CATEGORIA_DA_FEATURE.values())), 5)


class MetricasTest(unittest.TestCase):

    def test_matriz_e_incidentes(self):
        janelas = pd.DataFrame({
            "anomala": [True, True, False, False, True],
            "cenario": ["A", "A", None, None, "B"],
            "incidentes": [[1], [1], [], [], [2]],
        })
        d = modelos.Deteccao("teste", np.array([0.9, 0.2, 0.8, 0.1, 0.3]), np.array([True, False, True, False, False]))
        linhas = {(m["escopo"], m["unidade"]): m for m in avaliacao.avaliar(janelas, d, ["A", "B"], {1: "A", 2: "B"})}
        t = linhas[("todas", "janela")]
        self.assertEqual((t["vp"], t["fp"], t["fn"], t["vn"]), (1, 1, 2, 1))
        self.assertAlmostEqual(t["precisao"], 0.5)
        self.assertAlmostEqual(t["recall"], 1 / 3)
        self.assertAlmostEqual(linhas[("A", "janela")]["recall"], 0.5)
        self.assertEqual(linhas[("A", "incidente")]["vp"], 1, "uma janela marcada basta para o incidente")
        self.assertEqual(linhas[("B", "incidente")]["vp"], 0)
        self.assertAlmostEqual(linhas[("todos", "incidente")]["recall"], 0.5)

    def test_zscore_marca_o_ponto_extremo(self):
        X = pd.DataFrame({"a": [1.0] * 50 + [100.0], "constante": [2.0] * 51})
        d = modelos.zscore(X)
        self.assertTrue(d.marcada[-1])
        self.assertFalse(d.marcada[:-1].any())


if __name__ == "__main__":
    unittest.main()
