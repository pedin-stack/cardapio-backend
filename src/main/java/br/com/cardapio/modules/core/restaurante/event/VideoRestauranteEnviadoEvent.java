package br.com.cardapio.modules.core.restaurante.event;

import java.util.UUID;

/**
 * Fato: o gestor associou um vídeo original novo à capa do restaurante. Trafega só ids/caminho, nunca a entidade.
 *
 * @param restauranteId   restaurante dono do vídeo
 * @param caminhoOriginal caminho do original no storage
 */
public record VideoRestauranteEnviadoEvent(UUID restauranteId, String caminhoOriginal) {
}
