# ADR-0001 — Adoção do blueprint do Conecta e desvios deste projeto

- **Status:** Aceito
- **Data:** 2026-10-17

## Contexto

O backend do Cardápio segue o `conecta-backend-blueprint.md` (estrutura `config/ · infrastructure/ · modules/{modulo}/{dominio}/{camada}`,
pares `IService`/`Service`, DTOs record, `BusinessException` com status por constante, Flyway como dono do schema,
JWT stateless + RBAC por `@PreAuthorize`). O produto, porém, é bem menor que o Conecta e tem requisitos próprios
(vídeo em chunks, cliente anônimo).

## Decisão

Seguir o blueprint, com os desvios abaixo:

| # | Desvio | Por quê |
|---|---|---|
| 1 | **IAM enxuto**: sem Pessoa/CPF, cadastro público, verificação de e-mail, recuperação de senha nem troca de perfil. Usuário tem **um** perfil fixo (`Super Administrador` ou `Gestor Restaurante`) e, se gestor, um restaurante. Tabelas `perfis`/`permissao`/`perfil_permissoes` mantêm o formato do blueprint. | Só existem dois papéis e quem cria contas é o super admin. |
| 2 | **Primeiro super admin por `SuperAdministradorDataLoader`** (variáveis `SUPER_ADMIN_EMAIL`/`SUPER_ADMIN_SENHA`), não por SQL manual. | Sem cadastro público não há conta para promover. Nunca por migration (a senha não pode ir para o versionamento). |
| 3 | **Storage pela API REST do Supabase** (service-role key), não pelo protocolo S3. | Só a REST emite **URL de upload assinada**: o navegador envia direto ao bucket sem receber credencial de escrita; a mesma chave serve para baixar o original e gravar o HLS. |
| 4 | **Java 21** (blueprint: 17). | JDK 21 já instalado; LTS. |
| 5 | Pacote **`infrastructure`** (grafia correta). | Projeto novo, sem legado. |
| 6 | Cardápio público com **`Cache-Control: max-age=30`** em vez de cache Redis. | O DTO tem listas imutáveis (`Stream.toList()`) que o default typing do Jackson não desserializa; 30 s de atraso é aceitável. |
| 7 | `Restaurante` mora no **Core** (rotas `/api/v1/core/restaurantes`, permissões `CORE_RESTAURANTE_*`). | Usuários e cardápio dependem dele: o dado comum não nasce no módulo que o usa primeiro. |
| 8 | Dois composes: `docker-compose.yml` (dev) e `docker-compose.prod.yml` (VPS compartilhada, Caddy em container). | Um ambiente de produção; sem override/test. |
| 9 | **Supabase só para fotos.** O vídeo original passa pelo bucket (upload assinado) e é apagado após virar HLS; o HLS fica num volume da VPS, servido pela API em `/videos/**`. | O plano gratuito do Supabase (1 GB de storage, 5 GB de tráfego) não comporta vídeo; a VPS tem disco e banda. |
| 10 | Front na Vercel chama a API via **middleware** (`/api/*` → VPS). | O cookie de sessão fica no domínio do front: funciona com `*.vercel.app` + `*.sslip.io`, sem domínio próprio. |
| 11 | `/api/*` exige o segredo `X-Cardapio-Front` (injetado pelo middleware do front); Postgres só aceita a API (IP fixo no `pg_hba.conf`) e o socket local; Redis com senha; Swagger desligado em produção. | CORS só restringe navegadores; a API e o banco não devem atender nenhuma outra origem, nem processos da própria VPS. |

## Vídeo em chunks (HLS)

O original vai do navegador ao bucket; ao salvar o item, `VideoItemEnviadoEvent` dispara (após o commit, pool dedicado de 2
threads) o `VideoTranscodificacaoService`: FFmpeg gera HLS VOD com segmentos de **2 s**, keyframe forçado a cada 2 s,
duas qualidades (lado menor 480 px e 720 px) e `master.m3u8`, mais um pôster. O resultado só é gravado se o item ainda
apontar para o mesmo original (`UPDATE ... WHERE video_original_path = :original`), o que resolve a corrida de trocar o
vídeo durante a transcodificação. O front toca com hls.js numa janela de no máximo 3 players (ver o README do front).

## Consequências

- Recuperação de senha do gestor = o super admin define uma nova senha na edição do restaurante.
- HLS antigo sai do disco após o commit (vídeo trocado/removido, item ou restaurante excluído). Fotos órfãs no
  bucket (item excluído) ainda não são limpas.
- O FFmpeg roda no container da API: em escala, mover a transcodificação para um worker separado.
