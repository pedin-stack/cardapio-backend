package br.com.cardapio.modules.cardapio.itemcardapio.service;

import java.util.UUID;

/**
 * Contrato da transcodificação de vídeo para HLS (chunks de 2 s, 480p e 720p): do item e da capa do restaurante.
 */
public interface IVideoTranscodificacaoService {

    /**
     * Baixa o original, gera os segmentos HLS + pôster com FFmpeg, publica no storage e grava
     * PRONTO (ou ERRO) no item. Nunca lança: falha vira status ERRO + log.
     *
     * @param itemId          item dono do vídeo
     * @param caminhoOriginal caminho do original no storage
     */
    void transcodeVideoItem(UUID itemId, String caminhoOriginal);

    /**
     * Igual a {@link #transcodeVideoItem}, gravando o resultado no vídeo de fundo da capa do restaurante.
     *
     * @param restauranteId   restaurante dono do vídeo
     * @param caminhoOriginal caminho do original no storage
     */
    void transcodeVideoRestaurante(UUID restauranteId, String caminhoOriginal);
}
