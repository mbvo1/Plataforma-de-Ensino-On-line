# Evidências do Projeto 2

Os CSVs desta pasta são gerados por testes em
`sigea/dominio-principal/src/test/java/dev/com/sigea/dominio/` e em
`sigea/apresentacao-backend/src/test/`. Nenhum é editado à mão. A única
exceção é `fase4-demonstracao-console.txt`, a transcrição de uma demonstração
manual, que está marcada como tal. Este README não repete números de propósito: a fonte de
qualquer valor citado no relatório é o CSV, não este texto.

## Como regenerar

```bash
cd sigea
mvn test                                   # tudo, exceto a força bruta longa
mvn test -pl dominio-principal -Pexperimentos   # força bruta de 8 a 24 bits (leva minutos)
```

Os testes rodados pelo `mvn test` usam semente fixa e saem idênticos a cada
execução. A força bruta mede tempo de parede, então os segundos variam de
máquina para máquina e entre execuções; o número de tentativas e a chave
encontrada não variam. O mesmo vale para as colunas `ms_*` de
`seeder-volume.csv`; nesse arquivo, contagens e `hash_final` não variam.

## Fase 4: log encadeado

| O que | Onde |
|---|---|
| Tabela `log_seguranca` | `infraestrutura/.../db/migration/V28__criar_log_seguranca.sql` |
| Rótulos de verdade, à parte | `V29__criar_rotulo_evento_experimental.sql` |
| Hash e verificação | `dominio-principal/.../auditoria/CadeiaHashLog.java`, `VerificadorCadeiaLog.java` |
| Gravação (única porta de escrita) | `infraestrutura/.../auditoria/RegistradorLogSeguranca.java` |
| Seeder | `infraestrutura/.../auditoria/SeedEventosExperimentais.java` (gera com `GeradorEventosExperimentais`, no domínio) |
| Endpoints de admin | `apresentacao-backend/.../auditoria/AuditoriaSegurancaController.java` |

Endpoints, todos com `@PreAuthorize("hasRole('ADMINISTRADOR')")`:

- `GET /api/admin/auditoria/verificar-integridade`: devolve `OK` com `hashFinal`, ou
  `CORROMPIDO` com `logIdCorrompido`, `motivo` e `ultimoLogIdIntegro`. Cada
  verificação que encontra corrupção grava um `INTEGRIDADE_VIOLADA`.
- `GET /api/admin/logs/export?desde=&ate=&formato=csv|json`: `desde` é inclusivo,
  `ate` exclusivo, os dois em UTC e opcionais. A saída não traz rótulo.
- `POST /api/admin/anomalias`: recebe uma lista de
  `{usuarioId, inicioJanela, fimJanela, score, modelo}` e grava cada item como
  `ANOMALIA_DETECTADA`. Aceita no máximo 10 mil itens. Se um item for inválido,
  nada do lote é gravado.
- `GET /api/admin/experimento/rotulos?formato=csv|json`: a verdade conhecida, só
  para a avaliação da Fase 5.
- `POST /api/admin/experimento/seed?quantidade=&semente=`: vem **desligado**.
  Para ligar, use `sigea.experimento.seed.endpoint-habilitado=true`.

Seeder no Railway: perfil `seed-eventos` (`SPRING_PROFILES_ACTIVE=seed-eventos`).
Ele regrava os 100 mil eventos padrão a cada subida. O tamanho vai de 100 mil a
500 mil e é ajustado por `sigea.experimento.seed.quantidade`.

Demonstração manual do critério de aceite: suba o app com os perfis
`seed-eventos,console-h2` e rode `demo-fase4-console.sh`. Outra opção é abrir
`http://localhost:8080/h2-console` (URL `jdbc:h2:file:./sigea`, usuário `sa`,
senha vazia), editar uma linha e chamar o endpoint de verificação. O perfil
`console-h2` existe só para isso e nunca deve ir para o Railway.

## Arquivos

| Arquivo | Gerado por | O que contém |
|---|---|---|
| `difusao-comparacao.csv` | `DiagnosticoDifusaoTest` | Adendo 01: camada de difusão x permutação, 20.000 pares, 10 rodadas, bloco único |
| `difusao-ruins-por-byte.csv` | `DiagnosticoDifusaoTest` | Pares com menos de 32 bits alterados, por byte em que o bit foi virado |
| `avalanche.csv` | `AtaqueDiferencialTest` | Avalanche de texto e de chave por rodada (1 a 10, e 14 para o AES), bloco único |
| `avalanche-histograma.csv` | `AtaqueDiferencialTest` | Distribuição completa da distância de Hamming com o número padrão de rodadas |
| `avalanche-modo.csv` | `AtaqueDiferencialTest` | Efeito do modo de operação (CBC x GCM), separado da avalanche da cifra |
| `forca-bruta.csv` | `AtaqueForcaBrutaTest` (perfil `experimentos`) | Busca exaustiva real com 8, 12, 16, 20 e 24 bits desconhecidos |
| `forca-bruta-extrapolacao.csv` | `AtaqueForcaBrutaTest` | Ajuste exponencial e extrapolação para 2^256 (taxa medida e 10^12 chaves/s) |
| `forca-bruta-ritmo.csv` | `AtaqueForcaBrutaTest` | Tempo e coleta de lixo a cada 2^20 candidatas, para checar a estabilidade da taxa |
| `estatistico.csv` | `AtaqueEstatisticoTest` | Qui-quadrado, valor-p, entropia, índice de coincidência e blocos repetidos |
| `frequencia-bytes.csv` | `AtaqueEstatisticoTest` | Histograma de bytes do corpus: texto claro x CifraSigea x AES-256-GCM |
| `corpus-estatistico.csv` | `AtaqueEstatisticoTest` | Arquivos que compõem o corpus, tamanhos e SHA-256 do total |
| `cauda-diagnostico.csv` | `DiagnosticoCaudaTest` | Cauda residual da MixColumns: S-box (H1) x colunas fechadas pela permutação (H2), P livre e P atual |
| `cauda-pares-ruins.csv` | `DiagnosticoCaudaTest` | Cada par ruim, com a permutação, o confinamento e a uniformidade diferencial da sua chave |
| `estego.csv` | `ExperimentoEsteganografiaTest` | Fase 3: por capa e ocupação, payload, capacidade usada, PSNR e qui-quadrado LSB (imagem inteira e trecho embutido) |
| `estego-curva.csv` | `ExperimentoEsteganografiaTest` | Valor-p do qui-quadrado acumulado a cada 5% da imagem, na ordem do embutimento |
| `estego-capas.csv` | `ExperimentoEsteganografiaTest` | PNGs versionados usados como capa, dimensões, SHA-256 e duplicatas descartadas |
| `seeder-resumo.csv` | `GeradorEventosExperimentaisTest` | Fase 4: composição do volume padrão do seeder (100 mil eventos), usuários, incidentes, fração anômala |
| `seeder-tipos.csv` | `GeradorEventosExperimentaisTest` | Eventos por tipo e resultado, normais x rotulados como anômalos |
| `seeder-horas.csv` | `GeradorEventosExperimentaisTest` | Eventos por hora local (Recife), normais x anômalos |
| `seeder-dias-semana.csv` | `GeradorEventosExperimentaisTest` | Eventos por dia da semana, normais x anômalos |
| `seeder-incidentes.csv` | `GeradorEventosExperimentaisTest` | Cada incidente injetado: cenário, usuário, início, fim, duração e número de eventos |
| `seeder-volume.csv` | `LogSegurancaIntegracaoTest` (módulo `apresentacao-backend`) | Gravação real dos 100 mil eventos no H2 via Flyway: tempos e o hash final da cadeia |
| `fase4-demonstracao-console.txt` | `demo-fase4-console.sh`, execução manual | Transcrição da edição de uma linha pelo console H2 do app real e da resposta do endpoint |
| `deteccao.csv` | `analise-anomalias/detectar.py` | Fase 5: IsolationForest x z-score contra os rótulos. Precisão, recall, F1, matriz de confusão, ROC-AUC e precisão média; recall por cenário e por incidente |
| `deteccao-features.csv` | `analise-anomalias/detectar.py` | Média de cada feature por grupo (janelas normais e cada cenário) e a anomalia do enunciado que a feature cobre |
| `deteccao-zscore-feature-dominante.csv` | `analise-anomalias/detectar.py` | Feature que disparou o z-score, nos acertos e nos falsos positivos |
| `deteccao-isolation-forest-arvores.csv` | `analise-anomalias/detectar.py` | Em quantas árvores cada feature variou no treino e recebeu corte |
| `deteccao-entrada.csv` | `analise-anomalias/detectar.py` | Linhas e SHA-256 da exportação de eventos e dos rótulos usados |
| `fase5-execucao.txt` | `detectar.py --sigea ... --enviar`, execução manual | Transcrição da execução contra o app real, incluindo o envio das anomalias |

## Fase 5: detecção de anomalias

O código está em `analise-anomalias/`, na raiz do repositório. O README de lá
explica como rodar, as features e as decisões de método. Os CSVs `deteccao*`
não saem do `mvn test`: são gerados pelo script Python. Eles dependem da entrada
(cujo SHA-256 fica em `deteccao-entrada.csv`) e das versões fixadas em
`requirements.txt`. Com a mesma entrada, a saída é idêntica byte a byte. Rodar
contra o app gera uma entrada um pouco diferente a cada vez, porque o login do
próprio script entra no log com a hora real. A exportação (27 MB) não está
versionada. Na prática, entre uma execução com 100.001 eventos e outra com
100.002, só o ROC-AUC do IsolationForest mudou, de 0,9554 para 0,9555 (execuções
de 12/11/2026). Os números citados no relatório são os de `deteccao.csv`.

## Variantes da CifraSigea nos CSVs

A cifra mudou duas vezes; os CSVs comparam as três versões sobre os mesmos pares.

| Rótulo nos CSVs | Camada de difusão | Permutação P | Versão |
|---|---|---|---|
| `CIFRA_SIGEA_CADEIA_DUPLA_P_LIVRE` / `CADEIA_DUPLA,LIVRE` | cadeia dupla | livre (16!) | Fase 1 |
| `CIFRA_SIGEA_MDS_P_LIVRE` / `MIXCOLUMNS_MDS,LIVRE` | MixColumns | livre (16!) | Adendo 01 |
| `CIFRA_SIGEA_MDS` / `MIXCOLUMNS_MDS,ESPALHA_COLUNAS` | MixColumns | espalha cada coluna pelas quatro | atual (padrão) |

Por que a P mudou: com a P livre, algumas chaves devolvem os 4 bytes de uma
coluna para a mesma coluna, e a diferença fica presa em 32 bits em todas as
rodadas. `cauda-diagnostico.csv` mostra isso (linhas `LIVRE`) e mostra que a
qualidade da S-box não explica a cauda. A correção, `ModoPermutacao.ESPALHA_COLUNAS`,
foi decidida depois desse diagnóstico, não ajustada até o número melhorar; o
diagnóstico original continua reproduzível porque a P livre continua disponível.
Os CSVs de força bruta e estatístico usam só a cifra atual.

## Decisões de método que afetam a leitura

- **Avalanche na função de bloco, não no CBC.** O CBC só propaga para a
  frente; a média sobre mensagens longas mediria o modo e o tamanho da
  mensagem. O efeito do modo está à parte, em `avalanche-modo.csv`.
- **AES por rodada.** O JCE só oferece as 14 rodadas. A curva do AES vem de uma
  implementação de teste (`AesReferencia`) validada contra o vetor C.3 do
  FIPS-197 e contra o JCE. Convenção das rodadas reduzidas: a rodada final não
  tem MixColumns, como no FIPS-197.
- **Avalanche de chave da CifraSigea.** Virar um bit da chave mestra regenera
  a S-box e a permutação inteiras, então a avalanche de chave satura já na
  primeira rodada. Isso não é sinal de força; é consequência das tabelas
  dependentes de chave.
- **AES-256-GCM é um modo de fluxo.** Virar um bit do texto claro vira
  exatamente um bit do corpo cifrado; a difusão fica toda na tag. Não é
  fraqueza: a integridade do GCM vem da tag.
- **Corpus do ataque estatístico.** O brief pedia avisos e tópicos do seed do
  banco, mas esse seed não existe (nenhuma migration insere em `Avisos` ou
  `Topicos`). Por decisão do grupo, o corpus é o conjunto dos arquivos `.md`
  versionados no repositório (`git ls-files`), sem blocos de código e sem esta
  pasta. É português real, mas prosa técnica, não aviso de turma.
  Efeito colateral: versionar um `.md` novo muda o corpus. Também mudam as
  linhas `10KB_letra_A`, porque chave e nonces saem de um fluxo aleatório que
  o corpus consome antes. Foi o que aconteceu quando a Fase 5 versionou
  `analise-anomalias/README.md` (681 para 696 mensagens). `corpus-estatistico.csv`
  registra o SHA-256 do corpus de cada geração. As variações são de amostra
  única: com 255 graus de liberdade, um valor-p abaixo de 0,05 aparece em 5%
  das amostras de uma fonte uniforme. Um valor-p isolado não é sinal de viés.
- **Força bruta.** Uma thread, varredura completa do espaço (pior caso), com
  um par texto claro/texto cifrado conhecido como oráculo. Cada candidata da
  CifraSigea paga a geração da S-box e da permutação. As chaves por segundo
  variam muito de uma execução para outra na mesma máquina (o AES, que não
  muda, já variou perto de 2x); compare as duas cifras só dentro da mesma
  execução, nunca entre execuções.
- **Capas da esteganografia.** Todos os PNGs versionados, sem duplicatas.
  São capturas de tela: o repositório não tem fotografias. Tela tem áreas de
  cor chapada e histograma em picos, o caso mais fácil para o detector; o
  resultado não vale como estimativa para fotos.
- **Leitura do qui-quadrado LSB.** É a leitura inversa da Fase 2: valor-p
  perto de 1 significa pares (2k, 2k+1) igualados, ou seja, embutimento
  provável; perto de 0, imagem com aparência limpa. `nan` indica menos de dois
  pares com contagem suficiente para o teste.
- **Embutimento sequencial.** O payload ocupa o começo da imagem. Por isso o
  CSV traz o valor-p na imagem inteira e só no trecho embutido: um analista
  que olha a imagem inteira e outro que varre do começo veem coisas
  diferentes, e `estego-curva.csv` mostra a transição.
- **Hash do log cobre todas as colunas (desvio do brief, decidido pelo
  grupo).** A fórmula do brief deixava `ip` e `detalhe` fora do hash. Com ela,
  editar essas colunas pelo console passaria sem detecção, o que contradiz o
  critério de aceite. A demonstração manual editou justamente o `ip`. A
  serialização é canônica: cada campo vai com o comprimento na frente, e nulo
  fica diferente de vazio. Sem isso, dava para mover caracteres de um campo
  para outro sem mudar o hash.
- **Limites da cadeia, documentados e não corrigidos.** O SHA-256 não usa
  chave. Quem escreve no banco pode refazer todos os hashes a partir da linha
  alterada, e truncar o fim do log não quebra nenhum elo. Nos dois casos, só
  a âncora externa (o `hashFinal`, guardado fora do banco) denuncia.
  `CadeiaHashLogTest.limitesSemAncora` demonstra os dois. A correção seria usar
  HMAC com a chave fora do banco ou publicar o `hashFinal` periodicamente.
- **Linha apagada aponta a seguinte.** A linha apagada não existe mais para
  ser apontada. O endpoint devolve o `log_id` da linha seguinte e mostra o
  último íntegro, o que deixa o buraco visível.
- **`timestamp` em UTC.** As horas locais de `seeder-horas.csv` são de Recife
  (UTC-3, sem horário de verão). A Fase 5 precisa converter antes de calcular
  `hora_do_dia`.
- **Eventos com emissão real e eventos só do seeder.** Hoje o app emite de
  verdade `LOGIN_SUCESSO` e `LOGIN_FALHA` (nas rotas `/api/auth/login` e
  `/login-admin`, sem gravar o e-mail digitado), além de `ANOMALIA_DETECTADA` e
  `INTEGRIDADE_VIOLADA`. A CifraSigea ainda não é usada em nenhum fluxo do app,
  e não existe tela de esteganografia nem de ataques. Por isso `CIFRAGEM_*`,
  `DECIFRAGEM_*`, `ESTEGO_*` e `ATAQUE_EXECUTADO` só aparecem no volume
  sintético (decisão do grupo). Integrar a cifra a `AvisoEntity.conteudo`
  continua pendente; nenhuma fase do brief atribui essa tarefa.
- **IP dos logins reais.** O app grava `getRemoteAddr`, que no Railway é o IP
  do proxy. `X-Forwarded-For` não é usado porque pode ser forjado enquanto os
  proxies confiáveis não estiverem configurados.
- **Volume sintético.** O período é fixo: duas semanas a partir de 05/10/2026.
  Os usuários são sintéticos, com ids a partir de 100001, para não colidir com
  pessoas reais. Os IPs saem só dos blocos de documentação da RFC 5737, e o
  campo `detalhe` tem o mesmo formato em eventos normais e anômalos. Há falhas
  benignas: 7% das sessões começam com senha errada, e 0,3% das leituras falham.
  Sem elas, qualquer falha seria anomalia. `VIOLACAO_INTEGRIDADE` é separável
  por construção, porque `INTEGRIDADE_VIOLADA` nunca ocorre em uso normal. Os
  parâmetros foram fixados antes de qualquer detector rodar.
- **Seeder apaga o log.** Ele recomeça do `log_id` 1, então a mesma semente
  gera a mesma tabela, hash por hash: o `hash_final` de `seeder-volume.csv` se
  repete. Isso também apaga eventos reais anteriores, e por isso o endpoint
  vem desligado.
