#!/bin/bash

# =================================================================
# SCRIPT PARA RODAR O SCREENER DE FIIs
# =================================================================

# 1. Credenciais
# Use: export DB_PASSWORD='sua_senha' no terminal antes de rodar.
export DB_PASSWORD='SUA_SENHA_AQUI'
export BRAPI_TOKEN='SEU_TOKEN_DA_BRAPI_AQUI'

# 2. Compila e roda o projeto usando o wrapper do Maven (./mvnw)
echo "Iniciando a aplicação..."
chmod +x mvnw
./mvnw spring-boot:run
