#!/usr/bin/env bash
# Ensaio da apresentacao do Projeto 2, de ponta a ponta, contra o app real.
# Cada passo e o que sera mostrado ao vivo; o script existe para ensaiar e
# para ter o resultado esperado de cada passo antes de subir ao palco.
#
# Uso, numa pasta de trabalho vazia (o banco ./sigea.mv.db nasce nela):
#   JWT_SECRET=<32+ bytes> GEMINI_API_KEY=x CPF_ENCRYPTION_KEY=<qualquer> \
#     java -jar <repo>/sigea/apresentacao-backend/target/sigea-apresentacao-backend-1.0.0-SNAPSHOT.jar \
#     --server.port=18080 --spring.profiles.active=seed-eventos --spring.h2.console.enabled=true > app.log 2>&1 &
# (a flag liga o console H2 so nesta execucao local; no application.properties ele fica desligado)
#   bash <repo>/sigea/evidencias/projeto-2/demo-apresentacao.sh <repo>
#
# Usa o admin padrao do seed do banco (admin@sigea.com). O token JWT so
# existe em memoria durante o script.
set -u
REPO=${1:?informe a raiz do repositorio}
B=http://localhost:18080
ADMIN_EMAIL=admin@sigea.com
ADMIN_SENHA=admin123

for i in $(seq 1 240); do grep -q "Seeder:" app.log 2>/dev/null && break; sleep 0.5; done
echo "== 0. app no ar com o volume experimental"
grep "Seeder:" app.log | sed 's/.*Seeder:/Seeder:/'

TOKEN=$(curl -s -X POST $B/api/auth/login-admin -H 'Content-Type: application/json' \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"senha\":\"$ADMIN_SENHA\"}" | python -c "import sys,json;print(json.load(sys.stdin)['token'])")
H="Authorization: Bearer $TOKEN"

# Console H2 pela interface HTTP dele mesmo: o mesmo SQL que se cola na tela.
SID=$(curl -s -c cj.txt "$B/h2-console/" | grep -o 'jsessionid=[0-9a-f]*' | head -1 | cut -d= -f2)
curl -s -b cj.txt "$B/h2-console/login.do?jsessionid=$SID" \
  --data-urlencode "language=en" --data-urlencode "setting=Generic H2 (Embedded)" \
  --data-urlencode "name=Generic H2 (Embedded)" --data-urlencode "driver=org.h2.Driver" \
  --data-urlencode "url=jdbc:h2:file:./sigea" --data-urlencode "user=sa" --data-urlencode "password=" -o /dev/null
h2() { curl -s -b cj.txt "$B/h2-console/query.do?jsessionid=$SID" --data-urlencode "sql=$1"; }
# So os eventos da coluna de avisos: o volume do seeder tambem tem DECIFRAGEM_FALHA, sinteticos.
contar() { curl -s -H "$H" "$B/api/admin/logs/export?formato=csv" | cut -d, -f3,5 | grep -c "^$1,avisos.conteudo\$"; }
aviso() {
  curl -s -H "$H" "$B/api/avisos/historico?usuarioId=1" | PYTHONIOENCODING=utf-8 python -c "
import sys, json
for a in json.load(sys.stdin):
    # Bloco embaralhado pode trazer caractere de controle; vira '?' para nao sujar o terminal.
    if a['id'] == '$1': print(''.join(c if c.isprintable() else '?' for c in a['conteudo']))"
}

echo
echo "== 1. integridade da cadeia com o volume do seeder"
curl -s -H "$H" $B/api/admin/auditoria/verificar-integridade | cut -c1-120; echo

echo
echo "== 2. aviso novo: o banco guarda v1:Base64 da CifraSigea, a tela mostra o texto"
TEXTO="A prova de Calculo I foi remarcada para sexta-feira, 14h, sala 302. Tragam documento com foto."
ID=$(curl -s -X POST $B/api/avisos -H "$H" -H 'Content-Type: application/json' \
  -d "{\"titulo\":\"Prova remarcada\",\"conteudo\":\"$TEXTO\",\"autorId\":\"1\",\"disciplinaId\":\"1\",\"escopo\":\"GERAL\"}" \
  | python -c "import sys,json;print(json.load(sys.stdin)['id'])")
echo "aviso_id: $ID"
echo "SQL: SELECT conteudo FROM avisos WHERE aviso_id = $ID"
h2 "SELECT conteudo FROM avisos WHERE aviso_id = $ID" | grep -o 'v1:[A-Za-z0-9+/=]*' | head -1 | cut -c1-70
echo "tela de avisos: $(aviso $ID)"

echo
echo "== 3. fraqueza do CBC sem autenticacao: trocar 1 caractere no 1o bloco cifrado"
FALHAS=$(contar DECIFRAGEM_FALHA)
SQL="UPDATE avisos SET conteudo = CONCAT(LEFT(conteudo, 23), CASE WHEN SUBSTRING(conteudo, 24, 1) = 'A' THEN 'B' ELSE 'A' END, SUBSTRING(conteudo, 25)) WHERE aviso_id = $ID"
echo "SQL: $SQL"
h2 "$SQL" | grep -o 'Update count: [0-9]*' | head -1
echo "tela de avisos: $(aviso $ID)"
echo "DECIFRAGEM_FALHA de avisos.conteudo, antes/depois: $FALHAS/$(contar DECIFRAGEM_FALHA)  (nada acusou: o padding no fim ficou intacto)"

echo
echo "== 4. adulteracao que quebra o formato: cortar o texto cifrado"
SQL="UPDATE avisos SET conteudo = LEFT(conteudo, 30) WHERE aviso_id = $ID"
echo "SQL: $SQL"
h2 "$SQL" | grep -o 'Update count: [0-9]*' | head -1
echo "tela de avisos: $(aviso $ID)"
echo "DECIFRAGEM_FALHA de avisos.conteudo agora: $(contar DECIFRAGEM_FALHA)"

echo
echo "== 5. log encadeado: editar o ip do log_id 54321 a mao"
IP=$(h2 "SELECT ip FROM log_seguranca WHERE log_id = 54321" | grep -o '198\.51\.100\.[0-9]*\|203\.0\.113\.[0-9]*\|192\.0\.2\.[0-9]*' | head -1)
echo "ip original: $IP"
h2 "UPDATE log_seguranca SET ip = '10.66.66.66' WHERE log_id = 54321" | grep -o 'Update count: [0-9]*' | head -1
curl -s -H "$H" $B/api/admin/auditoria/verificar-integridade | cut -c1-160; echo
echo "desfazendo a edicao:"
h2 "UPDATE log_seguranca SET ip = '$IP' WHERE log_id = 54321" | grep -o 'Update count: [0-9]*' | head -1
curl -s -H "$H" $B/api/admin/auditoria/verificar-integridade | cut -c1-80; echo

echo
echo "== 6. deteccao em Python contra o log ao vivo, devolvendo as anomalias ao SIGEA"
SAIDA=$(mktemp -d)
( cd "$REPO/analise-anomalias" && SIGEA_ADMIN_EMAIL=$ADMIN_EMAIL SIGEA_ADMIN_SENHA=$ADMIN_SENHA \
    .venv/Scripts/python detectar.py --sigea $B --enviar --saida "$SAIDA" ) | grep -v "^\[Evidencias\]"
echo "-- metricas desta execucao (pasta temporaria; as versionadas nao foram tocadas)"
cut -d, -f1-9 "$SAIDA/deteccao.csv" | head -3

echo
echo "== 7. painel de seguranca le as anomalias e a cadeia"
curl -s -H "$H" "$B/api/admin/dashboard-seguranca/anomalias?porModelo=1" | cut -c1-200; echo
curl -s -H "$H" $B/api/admin/auditoria/verificar-integridade | cut -c1-80; echo
rm -f cj.txt
