# 0011. Comparar entidades por getId()

- **Status:** Aceito
- **Data:** 2026-10-17
- **Origem:** decisão escrita no código

## Contexto
`PersistenceEntity` usa igualdade por identidade.

## Decisão
Para saber se duas entidades são a mesma, compare `getId()`.

## Alternativas consideradas
Não registrado no código; confirmar com quem decidiu.

## Consequências
`equals` entre instâncias diferentes da mesma linha retorna falso.

## Como aplicar
Procurar `equals` aplicado a entidades nos services.

## Evidências
- `src/main/java/br/com/cardapio/infrastructure/model/PersistenceEntity.java:15` — "compare entidades por {@code getId()}"
