package br.com.cardapio.modules.core.perfil.enums;

/**
 * Permissões do sistema ({@code MODULO_ENTIDADE_ACAO[_ESCOPO]}).
 * <p>
 * Toda constante nova precisa de: (i) linha em {@code public.permissao} via migration,
 * (ii) concessão aos perfis via migration e (iii) espelho em {@code src/types/permissoes.ts} do front.
 * </p>
 */
public enum Permissao {

    // --- CORE: restaurantes (super admin) -----------------------------------------------------
    CORE_RESTAURANTE_VISUALIZAR_TODOS,
    CORE_RESTAURANTE_CRIAR,
    CORE_RESTAURANTE_ATUALIZAR,
    CORE_RESTAURANTE_APAGAR,

    // --- CORE: upload de mídia (URL assinada) -------------------------------------------------
    CORE_MIDIA_CRIAR,

    // --- CARDAPIO: categorias ------------------------------------------------------------------
    CARDAPIO_CATEGORIA_VISUALIZAR_PROPRIAS,
    CARDAPIO_CATEGORIA_CRIAR,
    CARDAPIO_CATEGORIA_ATUALIZAR,
    CARDAPIO_CATEGORIA_APAGAR,

    // --- CARDAPIO: itens -----------------------------------------------------------------------
    CARDAPIO_ITEM_VISUALIZAR_PROPRIOS,
    CARDAPIO_ITEM_CRIAR,
    CARDAPIO_ITEM_ATUALIZAR,
    CARDAPIO_ITEM_APAGAR,

    // --- CARDAPIO: métricas --------------------------------------------------------------------
    CARDAPIO_METRICA_VISUALIZAR_PROPRIAS
}
