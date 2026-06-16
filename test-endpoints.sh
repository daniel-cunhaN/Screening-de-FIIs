#!/bin/bash

# =================================================================
# SCRIPT DE TESTE DOS ENDPOINTS DE FIIs
# Equivalente a testar no Postman/Insomnia
# =================================================================
# Uso: ./test-endpoints.sh
# Certifique-se de que a aplicação está rodando (./run.sh)
# =================================================================

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color
BOLD='\033[1m'

separator() {
  echo ""
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo ""
}

print_request() {
  echo -e "${BOLD}${YELLOW}▶ $1 $2${NC}"
}

print_status() {
  local status=$1
  if [ "$status" -ge 200 ] && [ "$status" -lt 300 ]; then
    echo -e "${GREEN}✓ Status: $status OK${NC}"
  elif [ "$status" -ge 400 ] && [ "$status" -lt 500 ]; then
    echo -e "${RED}✗ Status: $status Client Error${NC}"
  elif [ "$status" -ge 500 ]; then
    echo -e "${RED}✗ Status: $status Server Error${NC}"
  else
    echo -e "${YELLOW}? Status: $status${NC}"
  fi
}

test_endpoint() {
  local method=$1
  local path=$2
  local description=$3

  separator
  echo -e "${BOLD}📋 Teste: ${description}${NC}"
  print_request "$method" "${BASE_URL}${path}"
  echo ""

  if [ "$method" = "GET" ]; then
    HTTP_RESPONSE=$(curl -s -w "\n%{http_code}" "${BASE_URL}${path}" 2>/dev/null)
  elif [ "$method" = "POST" ]; then
    HTTP_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}${path}" \
      -H "Content-Type: application/json" 2>/dev/null)
  fi

  HTTP_STATUS=$(echo "$HTTP_RESPONSE" | tail -1)
  HTTP_BODY=$(echo "$HTTP_RESPONSE" | head -n -1)

  if [ -z "$HTTP_STATUS" ] || [ "$HTTP_STATUS" = "000" ]; then
    echo -e "${RED}✗ Erro: Não foi possível conectar. A aplicação está rodando?${NC}"
    echo -e "${YELLOW}  Dica: Execute ./run.sh primeiro${NC}"
    return 1
  fi

  print_status "$HTTP_STATUS"
  echo ""
  echo -e "${BOLD}Response Body:${NC}"
  
  # Pretty-print JSON se 'jq' estiver disponível
  if command -v jq &>/dev/null; then
    echo "$HTTP_BODY" | jq '.' 2>/dev/null || echo "$HTTP_BODY"
  else
    echo "$HTTP_BODY"
  fi
}

# =================================================================
echo ""
echo -e "${BOLD}${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BOLD}${BLUE}║       🏢 TESTE DOS ENDPOINTS DE FIIs - API REST        ║${NC}"
echo -e "${BOLD}${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo -e "  Base URL: ${BASE_URL}"
echo -e "  Data: $(date '+%d/%m/%Y %H:%M:%S')"

# --- Teste 1: GET /api/fiis (Listar todos os FIIs) ---
test_endpoint "GET" "/api/fiis" "Listar todos os FIIs"

# --- Teste 2: GET /api/fiis/{ticker} (Buscar FII por ticker) ---
test_endpoint "GET" "/api/fiis/MXRF11" "Buscar FII por ticker (MXRF11)"

# --- Teste 3: GET /api/fiis/{ticker} (Ticker inexistente - deve retornar 404) ---
test_endpoint "GET" "/api/fiis/XXXX99" "Buscar FII inexistente (XXXX99) - Espera 404"

# --- Teste 4: POST /api/fiis/update (Atualizar dados dos FIIs) ---
test_endpoint "POST" "/api/fiis/update" "Trigger de atualização manual dos FIIs"

# =================================================================
separator
echo -e "${BOLD}${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BOLD}${BLUE}║                  📊 RESUMO DOS TESTES                   ║${NC}"
echo -e "${BOLD}${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  Endpoints testados:"
echo -e "    ${GREEN}GET${NC}  /api/fiis           → Lista todos os FIIs"
echo -e "    ${GREEN}GET${NC}  /api/fiis/{ticker}   → Busca FII por ticker"
echo -e "    ${GREEN}GET${NC}  /api/fiis/{ticker}   → Teste com ticker inválido (404)"
echo -e "    ${YELLOW}POST${NC} /api/fiis/update     → Trigger de atualização"
echo ""
echo -e "  ${BOLD}Dica:${NC} Instale 'jq' para visualização formatada do JSON"
echo -e "         sudo apt install jq"
echo ""
