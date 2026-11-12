# Evidências do Projeto 2

Todos os arquivos desta pasta são gerados por testes em
`sigea/dominio-principal/src/test/java/dev/com/sigea/dominio/cripto/`. Nenhum é
editado à mão. Este README não repete números de propósito: a fonte de
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
encontrada não variam.

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
