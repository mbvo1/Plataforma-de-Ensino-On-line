#!/usr/bin/env bash
# Demonstracao do criterio de aceite da Fase 4 contra o app real (jar),
# banco em arquivo, com a edicao feita pela interface HTTP do proprio console H2.
#
# Uso, numa pasta de trabalho vazia (o banco ./sigea.mv.db nasce nela):
#   JWT_SECRET=<32+ bytes> GEMINI_API_KEY=x CPF_ENCRYPTION_KEY=<qualquer> PORT=18080 \
#     java -jar sigea-apresentacao-backend-1.0.0-SNAPSHOT.jar \
#     --spring.profiles.active=seed-eventos,console-h2 > app.log 2>&1 &
#   bash <caminho>/demo-fase4-console.sh
# O token JWT so existe em memoria durante o script.
set -u
B=http://localhost:18080
for i in $(seq 1 120); do grep -q "Seeder:" app.log && break; sleep 0.5; done
echo "== log do seeder"; grep "Seeder:" app.log | sed 's/.*Seeder:/Seeder:/'

TOKEN=$(curl -s -X POST $B/api/auth/login-admin -H 'Content-Type: application/json' \
  -d '{"email":"admin@sigea.com","senha":"admin123"}' | python -c "import sys,json;print(json.load(sys.stdin)['token'])")
H="Authorization: Bearer $TOKEN"

echo "== 1. verificacao antes da edicao"
curl -s -H "$H" $B/api/admin/auditoria/verificar-integridade; echo

echo "== 2. edicao a mao pelo console H2 (login.do + query.do)"
PAGINA=$(curl -s -c cj.txt $B/h2-console/)
SID=$(curl -s -b cj.txt -c cj.txt "$B/h2-console/" | grep -o 'jsessionid=[0-9a-f]*' | head -1 | cut -d= -f2)
[ -z "$SID" ] && SID=$(echo "$PAGINA" | grep -o 'jsessionid=[0-9a-f]*' | head -1 | cut -d= -f2)
echo "sessao do console obtida: $([ -n "$SID" ] && echo sim || echo NAO)"
curl -s -b cj.txt "$B/h2-console/login.do?jsessionid=$SID" \
  --data-urlencode "language=en" --data-urlencode "setting=Generic H2 (Embedded)" \
  --data-urlencode "name=Generic H2 (Embedded)" --data-urlencode "driver=org.h2.Driver" \
  --data-urlencode "url=jdbc:h2:file:./sigea" --data-urlencode "user=sa" --data-urlencode "password=" -o /dev/null
SQL="UPDATE log_seguranca SET ip = '10.66.66.66' WHERE log_id = 54321"
echo "SQL: $SQL"
curl -s -b cj.txt "$B/h2-console/query.do?jsessionid=$SID" --data-urlencode "sql=$SQL" \
  | grep -o 'Update count: [0-9]*' | head -1

echo "== 3. verificacao depois da edicao"
curl -s -H "$H" $B/api/admin/auditoria/verificar-integridade; echo

echo "== 4. exportacao CSV completa (linhas incluindo cabecalho, bytes)"
curl -s -H "$H" "$B/api/admin/logs/export?formato=csv" -o export.csv
wc -l < export.csv; wc -c < export.csv
echo "== 5. evento INTEGRIDADE_VIOLADA registrado pela verificacao"
tail -1 export.csv | cut -d, -f1,3,5,7
