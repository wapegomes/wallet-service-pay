#!/bin/bash

# Cores para melhor visualização
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Testando API de Carteira Digital ===${NC}"

# Token JWT obtido anteriormente
TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTc1MTAyNTY0MSwiZXhwIjoxNzUxMTEyMDQxfQ.bY3fv_uTU2S_K1m8Xi4XFpC_wycYuTbdtdcvQVyTSP0"

# 1. Verificar saldo atual
echo -e "\n${BLUE}1. Verificando saldo atual:${NC}"
BALANCE_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets/testuser/balance)
echo $BALANCE_RESPONSE

# 2. Realizar um depósito
echo -e "\n${BLUE}2. Realizando depósito de R$ 150,00:${NC}"
DEPOSIT_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"idUsuario":"testuser","valor":150.00}' \
  http://localhost:8080/api/wallets/deposit)
echo $DEPOSIT_RESPONSE

# 3. Verificar saldo após depósito
echo -e "\n${BLUE}3. Verificando saldo após depósito:${NC}"
BALANCE_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets/testuser/balance)
echo $BALANCE_RESPONSE

# 4. Realizar um saque
echo -e "\n${BLUE}4. Realizando saque de R$ 50,00:${NC}"
WITHDRAW_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"idUsuario":"testuser","valor":50.00}' \
  http://localhost:8080/api/wallets/withdraw)
echo $WITHDRAW_RESPONSE

# 5. Verificar saldo após saque
echo -e "\n${BLUE}5. Verificando saldo após saque:${NC}"
BALANCE_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets/testuser/balance)
echo $BALANCE_RESPONSE

# 6. Criar outro usuário e carteira para teste de transferência
echo -e "\n${BLUE}6. Criando outro usuário para teste de transferência:${NC}"
SIGNUP_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{"username":"user2","email":"user2@example.com","password":"password123","roles":["USER"]}' \
  http://localhost:8080/api/auth/signup)
echo $SIGNUP_RESPONSE

# 7. Criar carteira para o segundo usuário
echo -e "\n${BLUE}7. Criando carteira para o segundo usuário:${NC}"
CREATE_WALLET_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"idUsuario":"user2"}' \
  http://localhost:8080/api/wallets)
echo $CREATE_WALLET_RESPONSE

# 8. Realizar uma transferência
echo -e "\n${BLUE}8. Realizando transferência de R$ 30,00 para user2:${NC}"
TRANSFER_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"idUsuarioOrigem":"testuser","idUsuarioDestino":"user2","valor":30.00}' \
  http://localhost:8080/api/wallets/transfer)
echo $TRANSFER_RESPONSE

# 9. Verificar saldo após transferência (usuário origem)
echo -e "\n${BLUE}9. Verificando saldo de testuser após transferência:${NC}"
BALANCE_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets/testuser/balance)
echo $BALANCE_RESPONSE

# 10. Verificar saldo após transferência (usuário destino)
echo -e "\n${BLUE}10. Verificando saldo de user2 após transferência:${NC}"
BALANCE_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets/user2/balance)
echo $BALANCE_RESPONSE

echo -e "\n${GREEN}Testes concluídos!${NC}"
