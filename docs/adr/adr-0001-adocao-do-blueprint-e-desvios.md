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
| 8 | Sem `docker-compose.override/prod/test`: um compose lendo `.env`. | Um ambiente por enquanto; separar quando houver deploy. |

## Vídeo em chunks (HLS)

O original vai do navegador ao bucket; ao salvar o item, `VideoItemEnviadoEvent` dispara (após o commit, pool dedicado de 2
threads) o `VideoTranscodificacaoService`: FFmpeg gera HLS VOD com segmentos de **2 s**, keyframe forçado a cada 2 s,
duas qualidades (lado menor 480 px e 720 px) e `master.m3u8`, mais um pôster. O resultado só é gravado se o item ainda
apontar para o mesmo original (`UPDATE ... WHERE video_original_path = :original`), o que resolve a corrida de trocar o
vídeo durante a transcodificação. O front toca com hls.js numa janela de no máximo 3 players (ver o README do front).

## Consequências

- Recuperação de senha do gestor = o super admin define uma nova senha na edição do restaurante.
- Vídeos antigos (HLS anterior a uma troca) ficam no bucket; limpeza é trabalho futuro.
- O FFmpeg roda no container da API: em escala, mover a transcodificação para um worker separado.
