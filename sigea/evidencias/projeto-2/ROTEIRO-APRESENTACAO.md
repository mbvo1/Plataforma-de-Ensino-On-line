# Roteiro da apresentação do Projeto 2

Cada passo foi ensaiado do zero em banco novo; o resultado esperado está em
`demo-apresentacao-saida.txt`. Os números para citar estão no fim, todos tirados dos CSVs.

## Antes de começar (30 minutos antes)

1. Gere o jar e suba o app numa pasta vazia, com o seeder e o console H2:

   ```bash
   cd sigea && mvn -q -DskipTests package
   mkdir -p ~/demo-sigea && cd ~/demo-sigea && rm -f sigea.mv.db
   JWT_SECRET=<32+ caracteres> GEMINI_API_KEY=x CPF_ENCRYPTION_KEY=<qualquer> \
     java -jar <repo>/sigea/apresentacao-backend/target/sigea-apresentacao-backend-1.0.0-SNAPSHOT.jar \
     --spring.profiles.active=seed-eventos,console-h2 > app.log 2>&1 &
   ```

   O app está pronto quando `app.log` mostrar `Seeder: 100000 eventos` e `Started`
   (perto de 30 s).

2. Deixe abertas, nesta ordem, as abas abaixo, já logadas como admin
   (`admin@sigea.com`):
   - `http://localhost:8080/login-admin.html`;
   - `/dashboard-seguranca.html`;
   - `/avisos-admin.html`;
   - `/esteganografia.html`;
   - `/h2-console`, com URL `jdbc:h2:file:./sigea`, usuário `sa` e senha vazia.

3. Tenha à mão um PNG (uma foto ou uma captura de tela) e um JPG qualquer.

4. Deixe um terminal aberto em `analise-anomalias/`, com a `.venv` instalada.

5. **Não use o perfil `console-h2` no Railway.** Ele existe só para a demonstração local.

## Roteiro (cerca de 15 minutos)

### 1. A cifra no banco (2 min)

- Em **Avisos**, publique um aviso, por exemplo "A prova de Cálculo I foi remarcada
  para sexta, 14h, sala 302."
- No console H2, rode:

  ```sql
  SELECT aviso_id, titulo, conteudo FROM avisos;
  ```

- **Resultado esperado:** `conteudo` começa com `v1:`, seguido de Base64, enquanto a tela
  mostra o texto normal.
- **O que dizer:** "o conteúdo vai para o banco cifrado pela nossa cifra, a CifraSigea.
  Cada gravação usa um nonce novo, e as subchaves dependem dele, por isso a chave nunca é
  fixa: o mesmo texto cifra diferente a cada vez."

### 2. A fraqueza do CBC, ao vivo (2 min)

Troque `1` pelo `aviso_id` do passo anterior:

```sql
UPDATE avisos SET conteudo = CONCAT(LEFT(conteudo, 23),
  CASE WHEN SUBSTRING(conteudo, 24, 1) = 'A' THEN 'B' ELSE 'A' END,
  SUBSTRING(conteudo, 25)) WHERE aviso_id = 1;
```

- Recarregue **Avisos**.
- **Resultado esperado:** os primeiros 16 caracteres viram lixo e uma letra logo depois
  muda ("Calculo I" vira "Calculo M" ou parecido). O resto do aviso fica intacto e
  **nenhum erro aparece**.
- **O que dizer:** "o CBC sem autenticação aceitou a adulteração calado. Esse é o achado
  da Parte 4. O CPF, que usa AES-GCM, recusaria isso pela tag."
- Depois, corte o texto cifrado:

  ```sql
  UPDATE avisos SET conteudo = LEFT(conteudo, 30) WHERE aviso_id = 1;
  ```

- **Resultado esperado:** o aviso aparece como `[conteudo indisponivel: falha na
  decifragem]`, e o log ganha um `DECIFRAGEM_FALHA`.
- **O que dizer:** "só detectamos a adulteração que quebra o formato ou o padding".

### 3. Log encadeado (2 min)

- No painel de segurança, mostre a cadeia **íntegra**, com cerca de 100 mil registros.
- No console H2, adultere uma linha:

  ```sql
  SELECT ip FROM log_seguranca WHERE log_id = 54321;   -- anote o valor
  UPDATE log_seguranca SET ip = '10.66.66.66' WHERE log_id = 54321;
  ```

- Clique em **Verificar agora**.
- **Resultado esperado:** **CORROMPIDO**, apontando o `log_id` 54321 (`HASH_DIVERGENTE`)
  e o último íntegro, 54320.
- Restaure o IP anotado e verifique de novo: volta a OK, agora com um
  `INTEGRIDADE_VIOLADA` registrado.
- **O que dizer:** "cada linha guarda o hash da anterior. Mexer em qualquer coluna quebra
  o elo exatamente ali. O limite é que quem escreve no banco poderia recalcular a cadeia
  inteira; por isso o hash final tem de ficar guardado fora do banco."

### 4. Esteganografia (2 min)

- Em **Esteganografia**, envie primeiro o JPG.
  - **Esperado:** recusa explícita, com a mensagem "JPEG recusado: a compressao com perda
    reescreve os bits menos significativos e destroi o payload. Use PNG."
- Depois envie o PNG, escolha o aviso e clique em **Cifrar e esconder**.
- Mostre as duas imagens lado a lado e os números:
  - PSNR bem acima de 40 dB;
  - qui-quadrado do trecho: o valor-p tende a ser baixo na original e perto de 1 com a
    mensagem (no ensaio com uma foto, 0,06 e 0,99).
- Baixe o PNG, envie em **Extrair** e mostre o texto voltando.
- **O que dizer:** "a imagem esconde que a mensagem existe; a cifra protege o conteúdo.
  Invisível ao olho, mas o qui-quadrado detecta onde a mensagem foi gravada."
- Escolha um aviso que ainda decifra. O aviso adulterado no passo 2 não serve para a
  extração.

### 5. Ataques e avalanche (3 min), no painel de segurança

- **Avalanche por rodada:** a CifraSigea chega a 50% na 2ª rodada e o AES na 3ª.
  - Clique na legenda para mostrar as versões antigas.
  - Conte a história da cauda: a cadeia dupla do brief deixava pares com 1 único bit
    alterado depois de 10 rodadas. Trocamos pela MixColumns depois de medir, e depois
    corrigimos a permutação com diagnóstico.
- **Distribuição de bytes:** o texto claro concentra-se em poucos valores; os dois textos
  cifrados ficam planos.
- **Força bruta:** cite os números da tabela abaixo; o gráfico está no relatório.

### 6. IA (3 min)

No terminal:

```bash
SIGEA_ADMIN_EMAIL=admin@sigea.com SIGEA_ADMIN_SENHA=admin123 \
  .venv/Scripts/python detectar.py --sigea http://localhost:8080 --enviar --saida /tmp/deteccao-demo
```

- **Use sempre `--saida`.** Sem ela, o script grava por cima dos CSVs versionados.
- Volte ao painel: a tabela de anomalias se enche, e o painel de métricas mostra os dois
  modelos.
- **O que dizer:**
  - "medimos contra a verdade conhecida que o seeder grava à parte, e não só olhando
    scores";
  - "o z-score, o modelo mais simples, ganhou em tudo";
  - "o IsolationForest perdeu metade das violações de integridade, porque essa feature é
    zero em quase toda a amostra de cada árvore".
- **Os números ao vivo diferem um pouco dos do relatório.** Os eventos da própria demo
  somam uma janela; o z-score muda 1 janela marcada, e o IsolationForest muda cerca de
  100. Isso mostra a instabilidade dele, e está no relatório.

## Se algo falhar

| Problema | O que fazer |
|---|---|
| App não sobe | Confira as três variáveis de ambiente. Mostre `demo-apresentacao-saida.txt`, que é o mesmo roteiro já executado |
| Gráficos não aparecem | O Chart.js vem de CDN. Sem internet, os gráficos mostram um aviso e as tabelas continuam funcionando |
| Console H2 não abre | Confira o perfil `console-h2` e a URL `jdbc:h2:file:./sigea`. Alternativa: rode `bash evidencias/projeto-2/demo-apresentacao.sh <repo>` noutro terminal |
| Python falha | Mostre `fase5-execucao.txt` e o painel de métricas, que lê o `deteccao.csv` versionado |
| Aviso adulterado não decifra mais | É o esperado depois do passo 2. Publique outro para o passo 4 |

## Números para citar

Todos saem dos CSVs desta pasta.

| O quê | Valor | Fonte |
|---|---|---|
| Avalanche de texto com 10 rodadas, CifraSigea | 50,01% (mín. 44 de 128 bits) | `avalanche.csv` |
| Rodada em que satura: CifraSigea / AES | 2ª / 3ª | `avalanche.csv` |
| Pares abaixo de 32 bits: cadeia dupla do brief / versão atual | 92 de 20.000 / 0 | `difusao-comparacao.csv` |
| Força bruta de 24 bits: CifraSigea / AES | 472,5 s / 46,4 s | `forca-bruta.csv` |
| 2^256 a 10^12 chaves/s | 3,67 × 10^57 anos | `forca-bruta-extrapolacao.csv` |
| Entropia: português / CifraSigea / AES | 5,02 / 7,9987 / 7,9986 bits por byte | `estatistico.csv` |
| Blocos repetidos em 10 KB de "A": ECB / CBC | 639 de 640 / 0 | `estatistico.csv` |
| PSNR com a imagem cheia (pior capa) | 51,13 dB | `estego.csv` |
| Eventos / incidentes / eventos rotulados | 100.000 / 50 / 2.899 | `seeder-resumo.csv` |
| z-score: recall / incidentes / precisão média | 0,9765 / 50 de 50 / 0,8533 | `deteccao.csv` |
| IsolationForest: recall / incidentes / precisão média | 0,9059 / 45 de 50 / 0,4165 | `deteccao.csv` |

## Perguntas prováveis

- **"Por que criar uma cifra própria se o AES existe?"** É o objeto de estudo do
  enunciado. Em produção usaríamos AES-GCM, e o SIGEA já usa no CPF. A comparação mostra
  o que falta na nossa: autenticação.
- **"A avalanche de chave satura na 1ª rodada. Isso é bom?"** Não é mérito: virar um bit
  da chave gera de novo a S-box e a permutação inteiras.
- **"Por que o valor-p do teste da letra A mudou entre versões?"** É amostra única. Com
  bytes uniformes, um valor-p abaixo de 0,05 aparece em 5% das vezes. Mudou a chave
  sorteada, não a cifra.
- **"A precisão de 4% a 5% não é ruim?"** É, e não escondemos isso. A base é de 0,38% de
  janelas anômalas, e os falsos positivos vêm de comportamento normal mas raro. Não
  mexemos no limiar para melhorar o número.
- **"O hash encadeado impede adulteração?"** Ele detecta adulteração parcial. Quem
  reescreve a cadeia toda ou corta o fim só é pego com o hash final guardado fora do
  banco. A correção seria HMAC com chave externa.
- **"Por que o hash cobre `ip` e `detalhe`, se o brief não pedia?"** Sem isso, editar o
  IP pelo console passaria sem detecção, o oposto do critério de aceite.
