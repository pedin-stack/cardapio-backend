# 0010. Repetir no service o limite de negócio já validado no DTO

- **Status:** Aceito
- **Data:** 2026-10-17
- **Origem:** decisão escrita no código

## Contexto
A regra de negócio não pode depender só da validação do controller.

## Decisão
O service confere o limite mesmo que o DTO já o valide (caso registrado: máximo de imagens por item).

## Alternativas consideradas
Confiar só no `@Size` do DTO.

## Consequências
Há duplicação proposital.

## Como aplicar
`ItemCardapioService` mantém a checagem de `ItemCardapio.MAXIMO_IMAGENS`.

## Evidências
- `src/main/java/br/com/cardapio/modules/cardapio/itemcardapio/service/ItemCardapioService.java:130` — "Redundante com o @Size do DTO de propósito"
