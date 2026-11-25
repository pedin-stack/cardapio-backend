package br.com.cardapio.modules.core.video.listener;

import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoVideoService;
import br.com.cardapio.modules.core.video.event.VideoItemDescartadoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Apaga do disco o HLS descartado — só depois do commit: se a alteração for desfeita, o vídeo continua no ar.
 */
@Component
@RequiredArgsConstructor
public class VideoItemDescartadoListener {

    private final IArmazenamentoVideoService armazenamentoVideoService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVideoItemDescartado(final VideoItemDescartadoEvent event) {
        armazenamentoVideoService.deleteHls(event.caminhoHls());
    }
}
