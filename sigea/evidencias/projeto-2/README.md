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
| `difusao-comparacao.csv` | `DiagnosticoDifusaoTest` | Adendo 01: cadeia dupla x MixColumns, 20.000 pares, 10 rodadas, bloco único |
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
| `cauda-diagnostico.csv` | `DiagnosticoCaudaTest` | Cauda residual da MixColumns: S-box (H1) x colunas fechadas pela permutação (H2) |
| `cauda-pares-ruins.csv` | `DiagnosticoCaudaTest` | Cada par ruim, com o confinamento e a uniformidade diferencial da sua chave |

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
  CifraSigea paga a geração da S-box e da permutação.
