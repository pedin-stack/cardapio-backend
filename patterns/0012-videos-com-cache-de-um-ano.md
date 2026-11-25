# 0012. Servir o HLS com cache de um ano, em pasta única por envio

- **Status:** Aceito
- **Data:** 2026-10-17
- **Origem:** decisão escrita no código

## Contexto
Cada envio de vídeo tem pasta com UUID próprio, então os arquivos nunca mudam.

## Decisão
`/videos/**` responde com cache de um ano.

## Alternativas consideradas
Não registrado no código; confirmar com quem decidiu.

## Consequências
Um arquivo publicado não pode ser sobrescrito: vídeo novo vai para pasta nova.

## Como aplicar
A transcodificação sempre grava em pasta com UUID novo.

## Evidências
- `src/main/java/br/com/cardapio/config/mvc/WebMvcConfig.java:14` — "os arquivos nunca mudam, então o cache é de 1 ano"
