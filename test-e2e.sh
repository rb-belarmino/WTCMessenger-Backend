#!/bin/bash

echo "========================================="
echo " E2E TEST: Auth + Messaging Microservices"
echo "========================================="

echo -e "\n1) Buscando Token de Operador no Auth Service (Porta 8080)..."
AUTH_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"operator@wtcmessenger.com","password":"operator123"}')

TOKEN=$(echo $AUTH_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken', ''))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
    echo "FALHA: Não foi possível obter o token. Resposta: $AUTH_RESPONSE"
    exit 1
fi
echo "SUCESSO: Token obtido! -> ${TOKEN:0:20}..."

echo -e "\n2) Enviando uma mensagem no Messaging Service (Porta 8082)..."
MESSAGE_PAYLOAD='{
  "recipientId": "customer123",
  "content": "Olá! Esta é uma mensagem de teste integrada.",
  "type": "TEXT"
}'

MESSAGE_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST http://localhost:8082/messages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$MESSAGE_PAYLOAD")

echo "$MESSAGE_RESPONSE" | grep -v HTTP_STATUS | python3 -m json.tool || echo "$MESSAGE_RESPONSE"

HTTP_STATUS=$(echo "$MESSAGE_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)

if [ "$HTTP_STATUS" == "200" ] || [ "$HTTP_STATUS" == "201" ]; then
    echo -e "\nSUCESSO: Mensagem enviada e enfileirada no Kafka!"
else
    echo -e "\nFALHA: Erro ao enviar mensagem. Status: $HTTP_STATUS"
fi

echo -e "\n3) Consultando as mensagens enviadas (se houver endpoint GET)..."
curl -s -X GET http://localhost:8082/messages/customer123 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool || echo "Endpoint GET /messages/{id} talvez não exista."

echo -e "\n========================================="
echo " TESTES FINALIZADOS"
echo "========================================="
