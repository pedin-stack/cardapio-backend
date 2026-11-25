# 0013. Manter as dependências no sentido cardapio, core, infrastructure

- **Status:** Proposta
- **Data:** 2026-11-25
- **Origem:** padrão inferido do código

## Contexto
A adequação de padrões moveu classes para que `core` não importe `cardapio` e `infrastructure` não importe módulos.

## Decisão
`modules/cardapio` pode importar `core`; `core` não importa `cardapio`; `infrastructure` não importa `modules`. O que os dois módulos usam sobe para o `core`.

## Alternativas consideradas
Não registrado no código; confirmar com quem decidiu.

## Consequências
A reação de um módulo de negócio a um fato do core é um listener no módulo de negócio. `config/` ainda importa o `core`.

## Como aplicar
Nenhum `import br.com.cardapio.modules.cardapio` em `modules/core`; nenhum `import br.com.cardapio.modules` em `infrastructure`.

## Evidências
- `src/main/java/br/com/cardapio/modules/cardapio/itemcardapio/listener/VideoRestauranteEnviadoListener.java`
- `src/main/java/br/com/cardapio/modules/core/video/`
- `src/main/java/br/com/cardapio/modules/core/shared/util/RestauranteLogadoUtil.java`
- Commit `d665965` (branch `refactor/adequar-padroes`)
