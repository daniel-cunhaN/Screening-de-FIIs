#!/bin/bash

# =================================================================
# SCRIPT PARA RODAR O SCREENER DE FIIs
# =================================================================

# 1. Credenciais
# Insira seus dados antes de rodar.
export DB_URL='jdbc:postgresql://ep-square-math-acq7j1zp-pooler.sa-east-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_BUHj6TOe0mEd&sslmode=require&channelBinding=require'
export DB_USERNAME='neondb_owner'
export DB_PASSWORD='npg_BUHj6TOe0mEd'
export BRAPI_TOKEN='vgHJmcstUXuuSU2o1id8Sc'

# 2. Compila e roda o projeto usando o wrapper do Maven (./mvnw)
echo "Iniciando a aplicação..."
chmod +x mvnw
./mvnw spring-boot:run
