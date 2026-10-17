package br.com.cardapio.modules.cardapio.itemcardapio.enums;

/**
 * Situação do vídeo do item no pipeline de transcodificação HLS.
 */
public enum StatusVideo {
    /** Item sem vídeo. */
    SEM_VIDEO,
    /** Original enviado; FFmpeg gerando os segmentos. */
    PROCESSANDO,
    /** HLS publicado: o cardápio público já toca. */
    PRONTO,
    /** Falha (duração acima do limite, arquivo inválido, storage fora). O gestor pode reenviar. */
    ERRO
}
