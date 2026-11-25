# 0001. Gravar datas no horário de Brasília, fixado antes do Spring subir

- **Status:** Aceito
- **Data:** 2026-10-19
- **Origem:** decisão escrita no código

## Contexto
O container roda em UTC. O relógio do Hibernate guarda o fuso do momento em que é criado, e aplicar o fuso só no `@PostConstruct` fazia `@CreationTimestamp`/`@UpdateTimestamp` gravarem em UTC.

## Decisão
O fuso padrão é `America/Sao_Paulo`, aplicado no `main` antes do Spring; o `@PostConstruct` fica para os testes.

## Alternativas consideradas
Aplicar o fuso apenas no `@PostConstruct` (descartado: gravava em UTC).

## Consequências
Datas `LocalDateTime` e métricas de horário ficam no horário local do restaurante. Os dados antigos foram convertidos pela migration `V20261019200000`.

## Como aplicar
`SpringTimezoneConfig.aplicarFusoPadrao()` é a primeira chamada do `main`; nenhum outro ponto troca o fuso padrão.

## Evidências
- `src/main/java/br/com/cardapio/config/time/SpringTimezoneConfig.java:12` — "Tem que valer antes do Hibernate subir"
- `src/main/java/br/com/cardapio/CardapioApplication.java:19` — `SpringTimezoneConfig.aplicarFusoPadrao();`
- `src/main/resources/db/migration/V20261019200000__datas_do_hibernate_de_utc_para_brasilia.sql:2`
