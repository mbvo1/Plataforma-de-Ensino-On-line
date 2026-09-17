#!/usr/bin/env bash
# ===========================================================================
# SIGEA - Seed de dados para a DEMONSTRACAO DE VULNERABILIDADES
# ---------------------------------------------------------------------------
# Popula o banco com o cenario minimo para demonstrar, pelo front-end:
#   - IA  : prompt injection no feedback automatico
#   - V-01: senhas em texto claro
#   - V-02: API sem autenticacao
#   - V-03: IDOR nos endpoints de aluno
#
# IMPORTANTE: este script usa a propria API SEM AUTENTICACAO para criar os
# dados. Isso ja e, por si so, uma demonstracao de V-02: nenhum token,
# cookie ou login e enviado em nenhuma das chamadas abaixo.
#
# Uso:
#   1. Suba a aplicacao (branch demo-apresentacao) em http://localhost:8080
#   2. Rode:  bash seed-demo.sh
#   (opcional) BASE_URL=http://localhost:8090 bash seed-demo.sh
#
# Rode uma vez sobre um banco limpo. Para reiniciar do zero, pare a app,
# apague apresentacao-backend/sigea.mv.db e suba de novo.
# ===========================================================================
set -u

BASE_URL="${BASE_URL:-http://localhost:8080}"
PDF_INJECTION="$(dirname "$0")/payloads/injection-comite-pedagogico.pdf"

# Credenciais fixas usadas na apresentacao
PROF_EMAIL="ada@sigea.com"
PROF_SENHA="senha123"          # senha padrao aplicada pelo backend
ALICE_EMAIL="alice@sigea.com"
ALICE_SENHA="alice123"
BRUNO_EMAIL="bruno@sigea.com"
BRUNO_SENHA="bruno123"
CODIGO_TURMA="DEMO01"

num() { grep -oE "$1"'"?:"?[0-9]+' | grep -oE '[0-9]+' | head -1; }

echo "==> Alvo: $BASE_URL"
echo "==> (todas as chamadas a seguir sao SEM autenticacao — isto e a V-02)"
echo

# 1) Professor -------------------------------------------------------------
echo "[1/6] Criando professor (Ada Lovelace)..."
RESP=$(curl -s -X POST "$BASE_URL/api/admin/professores" \
  -H "Content-Type: application/json" \
  -d "{\"nome\":\"Ada Lovelace\",\"email\":\"$PROF_EMAIL\",\"cpf\":\"11111111111\"}")
PROF_ID=$(echo "$RESP" | num '"id')
if [ -z "${PROF_ID:-}" ]; then echo "  ERRO: $RESP"; exit 1; fi
echo "  professorId=$PROF_ID  (login: $PROF_EMAIL / $PROF_SENHA)"

# 2) Turma -----------------------------------------------------------------
echo "[2/6] Criando turma (codigo $CODIGO_TURMA)..."
RESP=$(curl -s -X POST "$BASE_URL/api/professor/turmas?professorId=$PROF_ID" \
  -H "Content-Type: application/json" \
  -d "{\"nomeTurma\":\"Teoria dos Grafos - Demo\",\"codigoAcesso\":\"$CODIGO_TURMA\"}")
TURMA_ID=$(echo "$RESP" | num '"turmaId')
if [ -z "${TURMA_ID:-}" ]; then echo "  ERRO: $RESP"; exit 1; fi
echo "  turmaId=$TURMA_ID"

# 3) Atividade -------------------------------------------------------------
echo "[3/6] Criando atividade..."
RESP=$(curl -s -X POST "$BASE_URL/api/professor/turmas/$TURMA_ID/atividades?professorId=$PROF_ID" \
  -H "Content-Type: application/json" \
  -d "{\"titulo\":\"Trabalho Final - Grafos\",\"descricao\":\"Envie seu trabalho em PDF.\"}")
ATIV_ID=$(echo "$RESP" | num '"atividadeId')
if [ -z "${ATIV_ID:-}" ]; then echo "  ERRO: $RESP"; exit 1; fi
echo "  atividadeId=$ATIV_ID"

# 4) Alunos ----------------------------------------------------------------
# O registro devolve um id de dominio (UUID); o id numerico usado pelos
# endpoints /api/aluno/{id}/... vem do login. Por isso registramos e em
# seguida fazemos login para capturar o id numerico.
registrar_e_logar() {
  local nome="$1" email="$2" cpf="$3" senha="$4"
  curl -s -X POST "$BASE_URL/api/auth/registro" -H "Content-Type: application/json" \
    -d "{\"nome\":\"$nome\",\"email\":\"$email\",\"cpf\":\"$cpf\",\"senha\":\"$senha\"}" > /dev/null
  curl -s -X POST "$BASE_URL/api/auth/login" -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"senha\":\"$senha\"}" | num '"usuarioId'
}
echo "[4/6] Registrando alunos (Alice e Bruno)..."
ALICE_ID=$(registrar_e_logar "Alice Aluna" "$ALICE_EMAIL" "22222222222" "$ALICE_SENHA")
BRUNO_ID=$(registrar_e_logar "Bruno Aluno" "$BRUNO_EMAIL" "33333333333" "$BRUNO_SENHA")
if [ -z "${ALICE_ID:-}" ] || [ -z "${BRUNO_ID:-}" ]; then echo "  ERRO ao registrar alunos"; exit 1; fi
echo "  aliceId=$ALICE_ID  (login: $ALICE_EMAIL / $ALICE_SENHA)"
echo "  brunoId=$BRUNO_ID  (login: $BRUNO_EMAIL / $BRUNO_SENHA)"

# 5) Matricula na turma ----------------------------------------------------
echo "[5/6] Matriculando alunos na turma..."
curl -s -X POST "$BASE_URL/api/aluno/$ALICE_ID/turmas/entrar" -H "Content-Type: application/json" \
  -d "{\"codigoAcesso\":\"$CODIGO_TURMA\"}" > /dev/null
curl -s -X POST "$BASE_URL/api/aluno/$BRUNO_ID/turmas/entrar" -H "Content-Type: application/json" \
  -d "{\"codigoAcesso\":\"$CODIGO_TURMA\"}" > /dev/null
echo "  ok"

# 6) Alice envia o PDF com prompt injection --------------------------------
echo "[6/6] Alice enviando o PDF malicioso (payload de prompt injection)..."
if [ ! -f "$PDF_INJECTION" ]; then echo "  ERRO: PDF nao encontrado em $PDF_INJECTION"; exit 1; fi
RESP=$(curl -s -X POST "$BASE_URL/api/aluno/$ALICE_ID/atividades/$ATIV_ID/enviar" \
  -F "arquivo=@$PDF_INJECTION;type=application/pdf")
echo "  resposta: $RESP"

echo
echo "======================================================================"
echo " SEED CONCLUIDO"
echo "----------------------------------------------------------------------"
echo " Professor : $PROF_EMAIL / $PROF_SENHA   (id $PROF_ID)"
echo " Aluna A   : $ALICE_EMAIL / $ALICE_SENHA (id $ALICE_ID) -> enviou o PDF malicioso"
echo " Aluno B   : $BRUNO_EMAIL / $BRUNO_SENHA (id $BRUNO_ID)"
echo " Admin     : admin@sigea.com / admin123  (id 1)"
echo " Turma     : $CODIGO_TURMA (id $TURMA_ID)   Atividade: id $ATIV_ID"
echo "======================================================================"
