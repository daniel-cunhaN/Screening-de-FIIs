#!/bin/bash

# =================================================================
# SCRIPT PARA RODAR O SCREENER DE FIIs
# =================================================================

# 1. Credenciais
# Insira seus dados antes de rodar.
export DB_URL='Insira sua URL do banco (ex: jdbc:postgresql://host/neondb?sslmode=require)'
export DB_USERNAME='Insira seu username do banco'
export DB_PASSWORD='Insira sua senha do banco'
export BRAPI_TOKEN='Insira seu token da Brapi (opcional)'

# 2. Compila e roda o projeto usando o wrapper do Maven (./mvnw)
echo "Iniciando a aplicação..."
chmod +x mvnw
./mvnw spring-boot:run
