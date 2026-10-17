package br.com.cardapio.modules.core.restaurante.event;

import java.util.UUID;

/**
 * Fato: um restaurante foi excluído (o banco já apagou o cardápio em cascata; resta limpar arquivos).
 *
 * @param restauranteId restaurante excluído
 */
public record RestauranteExcluidoEvent(UUID restauranteId) {
}
