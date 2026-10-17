package br.com.cardapio.modules.cardapio.itemcardapio.service;

import java.util.UUID;

/**
 * Contrato da transcodificação do vídeo do item para HLS (chunks de 2 s, 480p e 720p).
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
}
