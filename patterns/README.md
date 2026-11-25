# Decisões do projeto

Só ADR com status **Aceito** vale como regra. **Proposta** é o retrato do código, aguardando confirmação.

| Nº | Decisão | Status | Data | Origem |
|---|---|---|---|---|
| 0001 | [Gravar datas no horário de Brasília, fixado antes do Spring subir](0001-horario-de-brasilia.md) | Aceito | 2026-10-19 | decisão escrita no código |
| 0002 | [Filtrar os dados do gestor pelo restaurante da sessão, nunca por id do cliente](0002-isolamento-pelo-restaurante-da-sessao.md) | Aceito | 2026-10-17 | decisão escrita no código |
| 0003 | [Entregar o JWT do painel em cookie HttpOnly](0003-jwt-em-cookie-httponly.md) | Aceito | 2026-10-17 | decisão escrita no código |
| 0004 | [Disparar efeitos em arquivos por evento, só depois do commit](0004-efeitos-em-arquivos-depois-do-commit.md) | Aceito | 2026-10-17 | decisão escrita no código |
| 0005 | [Identificar o cliente por CPF sem verificação e não expor os dados gravados](0005-cliente-identificado-por-cpf-sem-verificacao.md) | Aceito | 2026-10-19 | decisão escrita no código |
| 0006 | [Identificar curtidas e visualizações só pelo dispositivo](0006-anonimo-identificado-pelo-dispositivo.md) | Aceito | 2026-10-17 | decisão escrita no código |
| 0007 | [Aplicar rate limit antes do JWT, deixando os vídeos de fora](0007-rate-limit-e-ordem-dos-filtros.md) | Aceito | 2026-10-17 | decisão escrita no código |
| 0008 | [Reutilizar as permissões de item para adicionais e moderação de comentários](0008-adicionais-e-comentarios-usam-permissoes-de-item.md) | Aceito | 2026-10-19 | decisão escrita no código |
| 0009 | [Manter o projeto Supabase ativo com um ping diário](0009-ping-diario-no-supabase.md) | Aceito | 2026-11-09 | decisão escrita no código |
| 0010 | [Repetir no service o limite de negócio já validado no DTO](0010-regra-de-negocio-revalidada-no-service.md) | Aceito | 2026-10-17 | decisão escrita no código |
| 0011 | [Comparar entidades por getId()](0011-comparar-entidades-por-id.md) | Aceito | 2026-10-17 | decisão escrita no código |
| 0012 | [Servir o HLS com cache de um ano, em pasta única por envio](0012-videos-com-cache-de-um-ano.md) | Aceito | 2026-10-17 | decisão escrita no código |
| 0013 | [Manter as dependências no sentido cardapio, core, infrastructure](0013-dependencias-apontam-para-baixo.md) | Proposta | 2026-11-25 | padrão inferido do código |
| 0014 | [Responder 422 no controller a partir do BindingResult](0014-validacao-422-no-controller.md) | Proposta | 2026-10-17 | padrão inferido do código |
| 0015 | [Limpar o cache userDetails quando mudar o que decide a autenticação](0015-limpar-cache-userdetails-nas-mutacoes.md) | Proposta | 2026-10-17 | padrão inferido do código |

## Documentadas em outro lugar

Ver [`docs/adr/adr-0001-adocao-do-blueprint-e-desvios.md`](../docs/adr/adr-0001-adocao-do-blueprint-e-desvios.md):
adoção do blueprint (estrutura de pastas, pares `IService`/`Service`, DTOs record, `BusinessException`, Flyway dono do
schema, JWT stateless com `@PreAuthorize`), IAM enxuto, super admin por data loader, storage pela REST do Supabase,
Java 21, pacote `infrastructure`, `Cache-Control` no cardápio público, `Restaurante` no core, composes, bucket privado
com vídeo na VPS, middleware do front, segredo `X-Cardapio-Front`, especificações em JSONB e vídeo em HLS.
