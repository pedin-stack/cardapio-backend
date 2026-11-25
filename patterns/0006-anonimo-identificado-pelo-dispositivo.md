# 0006. Identificar curtidas e visualizações só pelo dispositivo

- **Status:** Aceito
- **Data:** 2026-10-17
- **Origem:** decisão escrita no código

## Contexto
O cliente do restaurante não tem conta.

## Decisão
Curtidas e tempo de tela são anônimos, identificados pelo `X-Device-ID` enviado pelo navegador.

## Alternativas consideradas
Não registrado no código; confirmar com quem decidiu.

## Consequências
O identificador é controlado pelo cliente: as métricas podem ser manipuladas.

## Como aplicar
Rotas de curtida e visualização são `permitAll` e não exigem identificação do cliente.

## Evidências
- `src/main/java/br/com/cardapio/config/security/WebSecurityConfig.java:55` — "anônimos, identificados só pelo dispositivoId"
- `src/main/resources/db/migration/cardapio/V20261017110100__cria_tabelas_curtidas_e_visualizacoes.sql:2`
