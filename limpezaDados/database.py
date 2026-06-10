import os
from dotenv import load_dotenv
from sqlalchemy import create_engine
import pandas as pd 

load_dotenv()
db_url = os.getenv("DATABASE_URL") # pega url do db no .env
engine = create_engine(db_url) # conecta o db

# --------------------------
# Arquitetura dos Dados
# --------------------------

df = pd.read_csv("limpezaDados/statusinvest-busca-avancada.csv", sep=';', decimal=',', thousands='.') # lê e trata os separadores, os decimais e os milhares.
colunas_uteis = ['TICKER', 'PRECO', 'DY', 'VALOR PATRIMONIAL COTA', 'PATRIMONIO'] 
df = df[colunas_uteis] # atribui colunas uteis ao dataframe

# Renomeando para ficar idêntico ao PostgreSQL #TODO Resolver se vou por o DY ou não (brapi)
df.rename(columns={
    'TICKER': 'ticker',
    'PRECO': 'preco_atual',
    'DY': 'dividend_yield', # * não tem no brapi
    'VALOR PATRIMONIAL COTA': 'valor_patrimonial_cota', #equity (brapi)
    'PATRIMONIO': 'patrimonio_liquido' #networth (brapi)
}, inplace=True)

#Cria tabela de data e atribui data de hoje 
df['data_referencia'] = pd.to_datetime('today').date() #! comentar futuramente

# Cria dados falsos para não ficar nulos
df['vacancia'] = 5.00     
df['setor'] = 'Setor (Teste)'  
df['tipo_fundo'] = 'Tipo Fundo Teste'  
# --------------------------
# Tratamento de Dados
# --------------------------

# Deleta valores duplicados e salva no DF
df = df.drop_duplicates() 

# Tratamento de nulos em colunas de Texto
colunas_texto = ['ticker', 'setor', 'tipo_fundo']
df[colunas_texto] = df[colunas_texto].fillna('NULO') 

# Tratamento de nulos em colunas Numéricas
colunas_numericas = ['dividend_yield', 'preco_atual', 'valor_patrimonial_cota', 'patrimonio_liquido', 'vacancia']
df[colunas_numericas] = df[colunas_numericas].fillna(0)

# Remove espaços no início e no fim das colunas que são de texto
df['ticker'] = df['ticker'].str.strip()
df['setor'] = df['setor'].str.strip()
df['tipo_fundo'] = df['tipo_fundo'].str.strip()

# --------------------------
# Separação de Dados
# --------------------------
df_ativos = df[['ticker', 'setor', 'tipo_fundo']]
df_indicadores = df[['ticker','data_referencia', 'dividend_yield', 'preco_atual', 'valor_patrimonial_cota', 'patrimonio_liquido', 'vacancia']]


# --------------------------
# Injeção dos Dados
# --------------------------
print(df_ativos.head(50))

try:
    df_ativos.to_sql(
        name='dim_ativos',    # Nome exato da tabela no db
        con=engine,           # conexão do db
        if_exists='append',   # 'append' adiciona novas linhas. 'replace' apagaria tudo e recriaria.
        index=False           # Impede que o índice do Pandas (0, 1, 2) vire uma coluna no SQL
    )
    print("Sucesso! df_ativos inseridos")

except Exception as e:
    print(f"Erro: {e}")

try:
    df_indicadores.to_sql(
        name='fato_indicadores',      # Nome exato da tabela no db
        con=engine,           # conexão do db
        if_exists='append',   # 'append' adiciona novas linhas. 'replace' apagaria tudo e recriaria.
        index=False           # Impede que o índice do Pandas (0, 1, 2) vire uma coluna no SQL
    )
    print("Sucesso! df_indicadores inseridos")

except Exception as e:
    print(f"Erro: {e}")