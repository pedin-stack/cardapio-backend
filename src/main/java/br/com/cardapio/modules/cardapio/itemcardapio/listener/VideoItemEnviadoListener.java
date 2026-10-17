package br.com.cardapio.modules.cardapio.itemcardapio.listener;

import br.com.cardapio.modules.cardapio.itemcardapio.event.VideoItemEnviadoEvent;
import br.com.cardapio.modules.cardapio.itemcardapio.service.IVideoTranscodificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Dispara a transcodificação depois do commit (o item já tem o caminho do original gravado)
 * no pool dedicado de vídeo — a requisição do gestor responde na hora com status PROCESSANDO.
 */
@Component
@RequiredArgsConstructor
public class VideoItemEnviadoListener {

    private final IVideoTranscodificacaoService videoTranscodificacaoService;

    @Async("videoTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVideoItemEnviado(final VideoItemEnviadoEvent event) {
        videoTranscodificacaoService.transcodeVideoItem(event.itemId(), event.caminhoOriginal());
    }
}
