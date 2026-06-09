import os
from dotenv import load_dotenv
from sqlalchemy import create_engine
import pandas as pd 

load_dotenv()
db_url = os.getenv("DATABASE_URL") # pega url do db no .env
engine = create_engine(db_url) # conecta o db

dados_mock = {
    'ticker': ['MXRF11', 'HGLG11'],
    'nome_fundo': ['Maxi Renda', 'CSHG Logística'],
    'setor': ['Papel', 'Galpões Logísticos']
}
df_fiis = pd.DataFrame(dados_mock)

try:
    df_fiis.to_sql(
        name='dim_fiis',      # Nome exato da tabela do DBeaver
        con=engine,           
        if_exists='append',   # 'append' adiciona novas linhas. 'replace' apagaria tudo e recriaria.
        index=False           # Vital: Impede que o índice do Pandas (0, 1, 2) vire uma coluna no SQL
    )
    print("Sucesso! Os FIIs foram inseridos no banco de dados na nuvem.")

except Exception as e:
    print(f"Ops, deu um erro: {e}")