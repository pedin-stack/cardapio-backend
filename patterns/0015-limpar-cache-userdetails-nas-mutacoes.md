# 0015. Limpar o cache userDetails quando mudar o que decide a autenticação

- **Status:** Proposta
- **Data:** 2026-10-17
- **Origem:** padrão inferido do código

## Contexto
O status do restaurante decide se o gestor continua autenticado.

## Decisão
Mutações de restaurante que afetam a autenticação usam `@CacheEvict` em `userDetails`.

## Alternativas consideradas
Não registrado no código; confirmar com quem decidiu.

## Consequências
O cache inteiro é limpo (`allEntries = true`).

## Como aplicar
Cadastro, atualização, status e exclusão de restaurante têm `@CacheEvict`.

## Divergência
O Javadoc diz que toda mutação limpa o cache, mas `updateAparenciaRestauranteLogado` não tem `@CacheEvict` (a aparência não muda dados de autenticação). Confirmar a regra e ajustar o comentário.

## Evidências
- `src/main/java/br/com/cardapio/modules/core/restaurante/service/RestauranteService.java:46` — "Toda mutação limpa o cache {@code userDetails}"
