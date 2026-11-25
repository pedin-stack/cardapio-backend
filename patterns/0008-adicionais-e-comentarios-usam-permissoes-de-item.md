# 0008. Reutilizar as permissões de item para adicionais e moderação de comentários

- **Status:** Aceito
- **Data:** 2026-10-19
- **Origem:** decisão escrita no código

## Contexto
O adicional faz parte da gestão do item.

## Decisão
Adicionais e moderação de comentários usam as authorities `CARDAPIO_ITEM_*`.

## Alternativas consideradas
Permissão própria, quando houver perfis que editem um e não o outro.

## Consequências
Não é possível conceder um acesso sem o outro.

## Como aplicar
`AdicionalController` e as rotas de gestor do `ComentarioController` usam `CARDAPIO_ITEM_*`.

## Evidências
- `src/main/java/br/com/cardapio/modules/cardapio/adicional/controller/AdicionalController.java:39` — "usa as permissões de item"
- `src/main/java/br/com/cardapio/modules/cardapio/comentario/controller/ComentarioController.java:39` — "a moderação usa as permissões de item"
