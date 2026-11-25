# 0002. Filtrar os dados do gestor pelo restaurante da sessão, nunca por id do cliente

- **Status:** Aceito
- **Data:** 2026-10-17
- **Origem:** decisão escrita no código

## Contexto
Um gestor não pode alcançar dados de outro restaurante (IDOR).

## Decisão
O id do restaurante vem do JWT (`RestauranteLogadoUtil`); arquivos enviados pelo gestor precisam estar na pasta do próprio restaurante.

## Alternativas consideradas
Não registrado no código; confirmar com quem decidiu.

## Consequências
O painel do gestor depende do `restauranteId` da sessão em toda consulta.

## Como aplicar
Em rotas do gestor, nenhum `restauranteId` vem de corpo, path ou query; busca por id usa também o restaurante; caminho de mídia passa por `CaminhoArquivoUtil.validateCaminhoOrThrow(caminho, pasta)`.

## Evidências
- `src/main/java/br/com/cardapio/modules/core/shared/util/RestauranteLogadoUtil.java:10` — "todo dado do gestor é filtrado pelo restaurante do JWT"
- `src/main/java/br/com/cardapio/modules/core/restaurante/service/RestauranteService.java:156` — "O restaurante vem da sessão, nunca do corpo: o gestor só muda o próprio."
