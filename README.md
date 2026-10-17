# Cardápio — API

API do cardápio social: cardápio público (sem login), painel do gestor do restaurante (itens, categorias, mídia,
métricas) e plataforma do super admin (restaurantes). Spring Boot 4 · Java 21 · PostgreSQL · Flyway · Redis · FFmpeg.

Padrões: `conecta-backend-blueprint.md`; desvios em [`docs/adr/adr-0001-adocao-do-blueprint-e-desvios.md`](docs/adr/adr-0001-adocao-do-blueprint-e-desvios.md).

## Subir

1. **Supabase**: crie um projeto e um bucket **público** `cardapio-midias` (Storage). Em *Project Settings › API* copie a
   URL e a `service_role` key. Se limitar MIME no bucket, libere `image/jpeg, image/png, image/webp, video/mp4,
   video/quicktime, video/webm, application/vnd.apple.mpegurl, video/mp2t`.
2. `cp .env.example .env` e preencha (`SUPABASE_*`, `SUPER_ADMIN_*`, segredos).
3. `docker compose up -d --build` → API em `http://localhost:8080`, Swagger em `/swagger-ui.html`.
4. Entre no front com `SUPER_ADMIN_EMAIL`/`SUPER_ADMIN_SENHA` (criado no 1º boot).

## Produção numa VPS com Caddy

O `docker-compose.prod.yml` não publica Postgres nem Redis, prende a API em `127.0.0.1`, limita CPU/RAM/logs
e confia nos cabeçalhos `X-Forwarded-*` do Caddy.

1. DNS: aponte `api.seudominio.com` (registro A) para o IP da VPS. O front deve ficar no mesmo domínio-pai
   (ex.: `app.seudominio.com`), senão o cookie de login não é enviado.
2. Na VPS: `git clone` deste repositório, `cp .env.example .env` e preencha com valores **novos** (segredo do JWT,
   senhas do banco e do super admin), `CORS_ALLOWED_ORIGINS=https://app.seudominio.com` e as chaves do Supabase.
3. `docker compose -f docker-compose.prod.yml up -d --build`
4. No Caddyfile (normalmente `/etc/caddy/Caddyfile`), acrescente — o HTTPS é automático:

   ```caddyfile
   api.seudominio.com {
   	encode zstd gzip
   	reverse_proxy 127.0.0.1:8080
   }
   ```

   e recarregue sem derrubar os outros sites: `sudo systemctl reload caddy` (ou `caddy reload`).
   Caddy em container? Veja o comentário `networks` no fim do `docker-compose.prod.yml` e use
   `reverse_proxy cardapio-api-1:8080`.
5. Conferir: `curl -I https://api.seudominio.com/swagger-ui.html` e `docker compose -f docker-compose.prod.yml logs -f api`.

Atualizar: `git pull && docker compose -f docker-compose.prod.yml up -d --build api` (as migrations rodam no boot).
Backup do banco: `docker compose -f docker-compose.prod.yml exec postgres pg_dump -U cardapio cardapio_db > backup.sql`.

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
