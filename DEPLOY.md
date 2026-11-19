# Deploy do Kaptu — Backend (Railway) + Frontend (Vercel)

Ordem: **1) banco/storage no Supabase → 2) backend no Railway → 3) frontend na Vercel → 4) voltar ao Railway e liberar o CORS**.

---

## 1. Supabase (banco + arquivos)

1. Em **Project Settings → Database → Connection string → JDBC**, copie a URL do *pooler em modo transação* (porta **6543**). Fica assim:
   `jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:6543/postgres?sslmode=require`
2. Usuário: `postgres.SEU_PROJECT_REF`; senha: a senha do banco.
3. Em **Storage**, crie o bucket `Documentos` (privado).
4. Em **Project Settings → API Keys**, copie a **secret key** (`sb_secret_...`) e a **Project URL** (`https://SEU_PROJECT_REF.supabase.co`).

## 2. Backend no Railway

1. Suba este projeto para o GitHub (repositório `back-definitivo`).
2. Railway → **New Project → Deploy from GitHub repo** → escolha `back-definitivo`.
   O Railway detecta o `Dockerfile` + `railway.json` sozinho (instala o Tesseract para o OCR; healthcheck em `/health`).
3. No serviço → **Variables**, cadastre:

| Variável | Valor |
|---|---|
| `DB_URL` | URL JDBC do passo 1.1 |
| `DB_USERNAME` | `postgres.SEU_PROJECT_REF` |
| `DB_PASSWORD` | senha do banco |
| `JPA_DDL_AUTO` | `update` no **primeiro** deploy (cria as tabelas); depois troque para `validate` |
| `SUPABASE_URL` | `https://SEU_PROJECT_REF.supabase.co` |
| `SUPABASE_SECRET_KEY` | `sb_secret_...` |
| `SUPABASE_STORAGE_BUCKET` | `Documentos` |
| `APP_CORS_ALLOWED_ORIGINS` | por enquanto `http://localhost:5173` (ajustado no passo 4) |
| `KAPTU_ADMIN_EMAIL` | (opcional) e-mail do admin inicial |
| `KAPTU_ADMIN_SENHA` | (opcional) senha com 12+ caracteres |

   **Não** defina `PORT`: o Railway define sozinho e a aplicação já usa (`server.port=${PORT:8080}`).
4. **Settings → Networking → Generate Domain**. Você recebe algo como `https://back-definitivo-production.up.railway.app`.
5. Teste: abra `https://SEU-BACKEND.up.railway.app/health` → deve mostrar `{"status":"UP"}`.

## 3. Frontend na Vercel

1. Suba o frontend para o GitHub (repositório `Front-definitivo`).
2. Vercel → **Add New → Project** → importe `Front-definitivo`.
   - Framework Preset: **Vite** (detectado)
   - Build Command: `npm run build` · Output Directory: `dist` (padrão)
3. Em **Environment Variables** (antes de clicar em Deploy):

| Variável | Valor |
|---|---|
| `VITE_API_URL` | `https://SEU-BACKEND.up.railway.app` (sem `/` no final e sem `/api/v1`) |
| `VITE_USE_MOCKS` | `false` |

4. **Deploy**. Anote o domínio, ex.: `https://front-definitivo.vercel.app`.

> As variáveis `VITE_*` entram no build. Se mudar alguma depois, faça **Deployments → ⋯ → Redeploy**.

## 4. Liberar o CORS (volta ao Railway)

No serviço do backend → **Variables** → edite `APP_CORS_ALLOWED_ORIGINS`:

```
https://front-definitivo.vercel.app,https://front-definitivo-*.vercel.app,http://localhost:5173
```

(troque `front-definitivo` pelo nome real do projeto na Vercel; o item com `*` libera os previews da Vercel). O Railway reinicia sozinho.

Depois de confirmar que tudo funciona, troque `JPA_DDL_AUTO` para `validate`.

## Checklist de problemas

| Sintoma | Causa provável |
|---|---|
| Front mostra "Não foi possível conectar ao servidor" | `VITE_API_URL` errada ou faltou Redeploy na Vercel |
| Erro de CORS no console do navegador | domínio da Vercel não está em `APP_CORS_ALLOWED_ORIGINS` (sem `/` no final) |
| Railway falha ao iniciar com erro de schema | primeiro deploy com `JPA_DDL_AUTO=validate`; use `update` uma vez |
| Upload/OCR retorna erro | `SUPABASE_URL` / `SUPABASE_SECRET_KEY` ausentes ou bucket com outro nome |
| Página dá 404 ao recarregar uma rota | `vercel.json` (rewrites) precisa estar no repositório — já está |
