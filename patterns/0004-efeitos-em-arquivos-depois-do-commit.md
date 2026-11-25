# 0004. Disparar efeitos em arquivos por evento, só depois do commit

- **Status:** Aceito
- **Data:** 2026-10-17
- **Origem:** decisão escrita no código

## Contexto
Se a transação for desfeita, o vídeo precisa continuar no ar, e a transcodificação precisa enxergar o caminho já gravado.

## Decisão
O service publica um evento; um `@TransactionalEventListener(AFTER_COMMIT)` apaga ou transcodifica. Os eventos de vídeo trafegam só ids e caminhos, nunca a entidade.

## Alternativas consideradas
Não registrado no código; confirmar com quem decidiu.

## Consequências
A resposta ao gestor sai antes da transcodificação (status `PROCESSANDO`).

## Como aplicar
Nenhum service apaga HLS nem inicia transcodificação direto; records de `event/` não têm campo de entidade.

## Evidências
- `src/main/java/br/com/cardapio/modules/core/video/listener/VideoItemDescartadoListener.java:11` — "se a alteração for desfeita, o vídeo continua no ar"
- `src/main/java/br/com/cardapio/modules/cardapio/itemcardapio/event/VideoItemEnviadoEvent.java:6` — "Trafega só ids/caminho, nunca a entidade."
- `src/main/java/br/com/cardapio/modules/core/restaurante/listener/RestauranteExcluidoListener.java:12`
- Ver também `docs/adr/adr-0001-adocao-do-blueprint-e-desvios.md`, seção "Vídeo em chunks (HLS)".
