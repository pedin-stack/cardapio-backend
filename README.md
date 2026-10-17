# Cardápio — API

API do cardápio social: cardápio público (sem login), painel do gestor do restaurante (itens, categorias, mídia,
métricas) e plataforma do super admin (restaurantes). Spring Boot 4 · Java 21 · PostgreSQL · Flyway · Redis · FFmpeg.

Padrões: `conecta-backend-blueprint.md`; desvios em [`docs/adr/adr-0001-adocao-do-blueprint-e-desvios.md`](docs/adr/adr-0001-adocao-do-blueprint-e-desvios.md).

## Subir

1. **Supabase** (só Storage): bucket **público** `cardapio-midias` com MIME `image/jpeg, image/png, image/webp,
   video/mp4, video/quicktime, video/webm` e limite de 50 MB. Fotos ficam lá; o vídeo original passa por lá só até
   virar HLS (depois é apagado) — o HLS fica no disco da API e é servido em `/videos/**`.
   Em *Project Settings › API* copie a URL e a `service_role` key.
2. `cp .env.example .env` e preencha (`SUPABASE_*`, `SUPER_ADMIN_*`, segredos).
3. `docker compose up -d --build` → API em `http://localhost:8080`, Swagger em `/swagger-ui.html`.
4. Entre no front com `SUPER_ADMIN_EMAIL`/`SUPER_ADMIN_SENHA` (criado no 1º boot).

## Produção (VPS compartilhada, Caddy em container)

O `docker-compose.prod.yml` não publica Postgres nem Redis, limita CPU/RAM/logs, liga a API (`cardapio-api`) à rede
do container do Caddy (`PROXY_NETWORK`) e confia nos cabeçalhos `X-Forwarded-*`. O front (Vercel) chama a API pela
própria Vercel (rewrite em `/api/*`), então o cookie de login funciona sem domínio próprio.

1. Endereço: sem domínio, use sslip.io — `cardapio-api.<IP-com-hifens>.sslip.io` já resolve para a VPS.
2. Na VPS, copie o código (repositório privado: `git archive HEAD | ssh root@VPS "tar -x -C /home/coelhovps/cardapio-backend"`),
   crie o `.env` com valores **novos** (JWT, senhas, chaves do Supabase), `PROXY_NETWORK`, `VIDEO_URL_BASE=https://<api>/videos`
   e `CORS_ALLOWED_ORIGINS=https://<front>.vercel.app`.
3. `docker compose -f docker-compose.prod.yml up -d --build`
4. Acrescente ao Caddyfile do Caddy existente (anexar com `cat >>`: o arquivo é montado por bind e editores que
   recriam o arquivo quebram a montagem) e recarregue sem derrubar os outros sites:

   ```caddyfile
   cardapio-api.169-58-57-18.sslip.io {
   	encode zstd gzip
   	reverse_proxy cardapio-api:8080
   }
   ```

   `docker exec <caddy> caddy validate --config /etc/caddy/Caddyfile && docker exec <caddy> caddy reload --config /etc/caddy/Caddyfile`
5. Conferir: `curl -I https://<api>/swagger-ui.html` e `docker compose -f docker-compose.prod.yml logs -f cardapio-api`.

Atualizar: copiar o código de novo e `docker compose -f docker-compose.prod.yml up -d --build cardapio-api` (migrations rodam no boot).
Backup do banco: `docker compose -f docker-compose.prod.yml exec cardapio-postgres pg_dump -U cardapio cardapio_db > backup.sql`.

## Desenvolvimento

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.7"   # o Maven do sistema aponta para o JRE 8
mvn clean verify                                     # unitários + MetricasIT (Testcontainers: precisa do Docker)
```

## Módulos

| Rota | Quem | O quê |
|---|---|---|
| `POST/DELETE /api/v1/autenticacao/sessoes/sessao` | público | login/logout (JWT em cookie HttpOnly `cardapio_token`) |
| `GET /api/v1/core/usuarios/usuario/meus-dados` | logado | sessão |
| `/api/v1/core/restaurantes` | super admin | CRUD + `PUT …/restaurante/{id}/status` |
| `POST /api/v1/core/midias/upload-assinado` | gestor/super admin | token de upload direto ao bucket |
| `/api/v1/cardapio/categorias`, `/api/v1/cardapio/itens` | gestor | cardápio do próprio restaurante |
| `GET /api/v1/cardapio/metricas` | gestor | tempo por item, mais curtidos, quase pedidos, mapa dia×hora |
| `GET /api/v1/cardapio/restaurantes/restaurante/{slug}` | público | cardápio (desativado → 404 "Serviço indisponível") |
| `PUT/DELETE …/{slug}/itens/item/{id}/curtida` · `POST …/{slug}/visualizacoes` | público (`X-Device-ID`) | curtidas e tempo de tela |
