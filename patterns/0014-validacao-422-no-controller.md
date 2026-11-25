# 0014. Responder 422 no controller a partir do BindingResult

- **Status:** Proposta
- **Data:** 2026-10-17
- **Origem:** padrão inferido do código

## Contexto
Os endpoints com corpo validado repetem o mesmo tratamento (16 ocorrências).

## Decisão
O método recebe `@Valid` e `BindingResult` e devolve 422 com `ResultError.getResultErrors`.

## Alternativas consideradas
Não registrado no código; confirmar com quem decidiu. O `GlobalExceptionHandler` também trata `MethodArgumentNotValidException` com 422.

## Consequências
Há repetição em cada controller.

## Como aplicar
Todo `@RequestBody @Valid` é seguido de `BindingResult` e da checagem `hasErrors()`.

## Evidências
- `src/main/java/br/com/cardapio/modules/cardapio/categoria/controller/CategoriaController.java`
- `src/main/java/br/com/cardapio/modules/core/restaurante/controller/RestauranteController.java`
- `src/main/java/br/com/cardapio/modules/core/autenticacao/controller/AutenticacaoController.java`
