# 0003. Entregar o JWT do painel em cookie HttpOnly

- **Status:** Aceito
- **Data:** 2026-10-17
- **Origem:** decisão escrita no código

## Contexto
O JavaScript do front não vê o token da sessão.

## Decisão
O login grava o JWT em cookie HttpOnly e o corpo devolve só os dados do usuário; o logout expira o cookie pelo back.

## Alternativas consideradas
Não registrado no código; confirmar com quem decidiu.

## Consequências
Só o `Set-Cookie` do back apaga a sessão. `secure` depende de `API_SECURITY_COOKIE_SECURE` (HTTPS).

## Como aplicar
Nenhum DTO de resposta de sessão do painel carrega o token.

## Evidências
- `src/main/java/br/com/cardapio/modules/core/autenticacao/controller/AutenticacaoController.java:35` — "o JWT é entregue em cookie HttpOnly (o JS do front nunca o vê)"
- `src/main/java/br/com/cardapio/modules/core/autenticacao/controller/AutenticacaoController.java:73` — "só o Set-Cookie do back consegue apagar um cookie HttpOnly"
