package br.com.cardapio.modules.core.video.event;

/**
 * Fato: um HLS deixou de ser usado (vídeo trocado/removido ou item excluído) e pode sair do disco.
 *
 * @param caminhoHls caminho do master.m3u8 descartado
 */
public record VideoItemDescartadoEvent(String caminhoHls) {
}
