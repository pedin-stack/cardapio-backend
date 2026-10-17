package br.com.cardapio.modules.cardapio.itemcardapio.event;

import java.util.UUID;

/**
 * Fato: o gestor associou um vídeo original novo a um item. Trafega só ids/caminho, nunca a entidade.
 *
 * @param itemId          item dono do vídeo
 * @param caminhoOriginal caminho do original no storage
 */
public record VideoItemEnviadoEvent(UUID itemId, String caminhoOriginal) {
}
