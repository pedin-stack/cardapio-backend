package br.com.cardapio.modules.core.perfil.enums;

/**
 * Fonte única dos módulos do sistema. O prefixo de toda {@link Permissao} é o nome da constante + "_".
 */
public enum Modulo {

    /** Usuários, perfis, mídia e restaurantes (plataforma do super admin). */
    CORE,

    /** Cardápio do restaurante: categorias, itens, curtidas, visualizações e métricas. */
    CARDAPIO
}
