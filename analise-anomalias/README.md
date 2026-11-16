# Detecção de anomalias no log de segurança do SIGEA

Fase 5 do Projeto 2. Lê o log encadeado pela API de admin do SIGEA e agrega os
eventos em janelas de 5 minutos por usuário. Roda dois detectores, IsolationForest
e uma linha de base por z-score, e mede os dois contra a verdade conhecida que o
seeder grava. No fim, devolve as janelas marcadas ao SIGEA como `ANOMALIA_DETECTADA`.

## Como rodar

```bash
cd analise-anomalias
python -m venv .venv
.venv/Scripts/python -m pip install -r requirements.txt   # Linux/macOS: .venv/bin/python

# SIGEA rodando com o perfil seed-eventos (100 mil eventos rotulados)
export SIGEA_ADMIN_EMAIL=admin@sigea.com
export SIGEA_ADMIN_SENHA=...          # ou deixe vazio e digite no prompt
.venv/Scripts/python detectar.py --sigea http://localhost:8080 --enviar

# Na demonstração ao vivo: resultados numa pasta à parte, sem tocar nas evidências
.venv/Scripts/python detectar.py --sigea http://localhost:8080 --enviar --saida /tmp/deteccao-demo

# Ou sobre exportações já baixadas, sem o app e sem enviar nada
.venv/Scripts/python detectar.py --eventos dados/log-seguranca.csv --rotulos dados/rotulos.csv

# Testes
.venv/Scripts/python -m unittest discover -s tests
```

As credenciais nunca vão como argumento de linha de comando, porque ficariam no
histórico do shell. As exportações baixadas ficam em `dados/`, fora do git.

Os resultados são gravados em `sigea/evidencias/projeto-2/` (ou na pasta de
`--saida`). Use `--saida` sempre que o log tiver eventos além do volume do
seeder: as janelas mudam, e gravar por cima trocaria os números versionados
pelos de uma execução que ninguém versionou.

| Arquivo | Conteúdo |
|---|---|
| `deteccao.csv` | Precisão, recall, F1, matriz de confusão, ROC-AUC e precisão média por modelo; recall por cenário e por incidente |
| `deteccao-features.csv` | Média de cada feature em janelas normais e em cada cenário, com a anomalia do enunciado que a feature cobre |
| `deteccao-zscore-feature-dominante.csv` | A feature que disparou o z-score, nos acertos e nos falsos positivos |
| `deteccao-isolation-forest-arvores.csv` | Em quantas das árvores do IsolationForest cada feature variou no treino e recebeu algum corte |
| `deteccao-entrada.csv` | Linhas e SHA-256 dos dois CSVs de entrada |

Com a mesma entrada, a saída é idêntica byte a byte: a semente do IsolationForest
é fixa.

## Features

Cada uma das cinco anomalias do enunciado tem pelo menos uma feature:

| Anomalia do enunciado | Features |
|---|---|
| Número incomum de tentativas de autenticação | `n_tentativas_login`, `n_falhas_login`, `taxa_falha` |
| Frequência elevada de falhas de descriptografia | `n_falhas_decifragem` |
| Alterações inesperadas em arquivos | `n_violacoes_integridade` |
| Padrões atípicos de acesso | `n_recursos_distintos`, `raridade_hora` |
| Comportamentos incomuns na geração de eventos | `n_eventos`, `intervalo_medio_entre_eventos`, `desvio_intervalo` |

Decisões que afetam os números, todas tomadas antes da primeira execução:

- **Hora do dia vira `raridade_hora`.** O valor é `-ln(fração das janelas naquela
  hora local)`. A hora crua é circular (23h fica ao lado de 0h) e bimodal (picos
  de manhã e de tarde), então nem ela nem o z-score dela dizem "horário incomum".
  O log está em UTC; a hora é a de Recife (UTC-3).
- **Janela com poucos eventos.** Uma janela com um evento só recebe intervalo
  médio de 300 s, a duração da janela. O desvio exige pelo menos dois intervalos;
  abaixo disso, recebe a mediana das janelas que têm. Sem essa regra, toda janela
  com dois eventos teria desvio 0 e pareceria um robô.
- **Evento sem `usuario_id` é agrupado pelo IP.** Isso cobre a força bruta contra
  e-mails inexistentes. Essas janelas voltam ao SIGEA com `usuarioId` nulo.
- **`ANOMALIA_DETECTADA` não entra na entrada**, porque é a saída do próprio
  detector. Rodar de novo depois do envio dá as mesmas janelas.
- **IsolationForest com os padrões do scikit-learn** (`contamination='auto'`).
  Passar a taxa real de anomalias seria entregar o rótulo ao modelo.
- **z-score com |z| > 3 em qualquer feature.** Média e desvio são calculados sobre
  todas as janelas, sem rótulo.
- **Janela anômala é a que contém ao menos um evento rotulado.** Um incidente que
  cruza a fronteira de duas janelas conta nas duas. Um incidente é detectado
  quando pelo menos uma janela dele foi marcada.

## Leitura dos resultados

Os números estão em `deteccao.csv`; este texto não os repete. Para lê-los:

- **A precisão é baixa nos dois modelos, e isso é esperado.** As janelas anômalas
  são uma fração pequena do total (`positivos_reais` / `total`). Até uma taxa de
  falsos positivos pequena gera mais alarmes falsos que verdadeiros. ROC-AUC e
  precisão média medem o ranking sem depender do limiar escolhido.
- **O z-score é uma linha de base, não um espantalho.** Se ele empatar com o
  IsolationForest ou ganhar dele, isso é o resultado. Neste volume, os cenários
  injetados mexem em uma ou duas features de cada vez, e isso favorece a regra
  "alguma feature passou de 3 desvios".
- **O IsolationForest erra `VIOLACAO_INTEGRIDADE`, e o motivo foi verificado.**
  `n_violacoes_integridade` vale zero em toda janela normal. Cada árvore treina
  com 256 janelas sorteadas, então na maioria delas a feature fica constante e
  não recebe corte (`deteccao-isolation-forest-arvores.csv`). As janelas de
  violação que ele marca caíram em hora rara: o modelo as pegou pelo horário,
  não pela violação. É uma limitação conhecida do método com sinais raros e
  quase categóricos; uma regra fixa ("qualquer violação é alerta") resolveria
  esse caso.
- **De onde vêm os falsos positivos do z-score** está em
  `deteccao-zscore-feature-dominante.csv`: erro de senha benigno (`taxa_falha`),
  uso de madrugada (`raridade_hora`) e janelas com ritmo irregular. Foram
  analisados, não ajustados: o limiar continua 3.
- **O volume é sintético.** Os cenários foram desenhados com uma assinatura por
  anomalia (ver `GeradorEventosExperimentais`), e os números não valem como
  estimativa para tráfego real.
