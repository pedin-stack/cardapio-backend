# 0005. Identificar o cliente por CPF sem verificação e não expor os dados gravados

- **Status:** Aceito
- **Data:** 2026-10-19
- **Origem:** decisão escrita no código

## Contexto
Comentar e avaliar exigem identificação, e não há verificação por SMS.

## Decisão
Há um cadastro por CPF em cada restaurante; quem informa o CPF assume o cadastro. A API não devolve dados já gravados, e comentários mostram só o primeiro nome.

## Alternativas consideradas
Verificação por SMS (não implementada).

## Consequências
Quem souber um CPF pode assumir o cadastro; o último nome e telefone informados valem.

## Como aplicar
Nenhum DTO de resposta pública ou de comentário expõe CPF, telefone ou sobrenome já gravados.

## Evidências
- `src/main/java/br/com/cardapio/modules/cardapio/cliente/service/ClienteService.java:42` — "sem verificação por SMS, quem informa o CPF assume o cadastro"
- `src/main/java/br/com/cardapio/modules/cardapio/cliente/dto/response/ClienteSessaoResponseDTO.java:8` — "nunca dados já gravados de quem tinha aquele CPF"
- `src/main/java/br/com/cardapio/modules/cardapio/comentario/dto/response/ComentarioResponseDTO.java:10` — "telefone e CPF nunca saem"
