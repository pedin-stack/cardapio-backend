# 0009. Manter o projeto Supabase ativo com um ping diário

- **Status:** Aceito
- **Data:** 2026-11-09
- **Origem:** decisão escrita no código

## Contexto
O plano gratuito pausa o projeto após 7 dias sem uso, e então nenhuma foto é assinada.

## Decisão
Um list diário no bucket, às 6h de Brasília.

## Alternativas consideradas
Plano pago.

## Consequências
Se a API ficar 7 dias fora do ar, o projeto pausa mesmo assim.

## Como aplicar
O `@Scheduled manterProjetoAtivo` existe enquanto o plano for o gratuito.

## Evidências
- `src/main/java/br/com/cardapio/infrastructure/armazenamento/SupabaseArmazenamentoService.java:138` — "pausa o projeto após 7 dias sem uso"
- `src/main/java/br/com/cardapio/infrastructure/armazenamento/SupabaseArmazenamentoService.java:141` — "se a API ficar 7 dias fora do ar, o projeto pausa mesmo assim"
