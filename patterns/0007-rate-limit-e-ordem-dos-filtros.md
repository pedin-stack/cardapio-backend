# 0007. Aplicar rate limit antes do JWT, deixando os vídeos de fora

- **Status:** Aceito
- **Data:** 2026-10-17
- **Origem:** decisão escrita no código

## Contexto
O player pede dezenas de chunks por minuto sem `X-Device-ID`; contado por IP, o wi-fi de um restaurante estouraria o limite.

## Decisão
Ordem dos filtros: origem do front, rate limit, JWT. O limite é por `X-Device-ID` ou IP (100 req/min) e não vale para `/videos`.

## Alternativas consideradas
Não registrado no código; confirmar com quem decidiu.

## Consequências
Não é proteção contra DDoS, porque o header é controlado pelo cliente.

## Como aplicar
Conferir a ordem em `WebSecurityConfig` e a exclusão de `/videos` em `RateLimitFilter`.

## Evidências
- `src/main/java/br/com/cardapio/config/security/WebSecurityConfig.java:68` — "Ordem: 1º só o front passa → 2º rate limit → 3º JWT."
- `src/main/java/br/com/cardapio/config/security/RateLimitFilter.java:17` — "não é proteção contra DDoS"
- `src/main/java/br/com/cardapio/config/security/RateLimitFilter.java:28`
