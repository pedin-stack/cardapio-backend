package br.com.cardapio.modules.core.restaurante.listener;

import br.com.cardapio.modules.cardapio.itemcardapio.service.IVideoTranscodificacaoService;
import br.com.cardapio.modules.core.restaurante.event.VideoRestauranteEnviadoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Dispara a transcodificação do vídeo da capa depois do commit, no mesmo pool de vídeo dos itens.
 */
@Component
@RequiredArgsConstructor
public class VideoRestauranteEnviadoListener {

    private final IVideoTranscodificacaoService videoTranscodificacaoService;

    @Async("videoTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVideoRestauranteEnviado(final VideoRestauranteEnviadoEvent event) {
        videoTranscodificacaoService.transcodeVideoRestaurante(event.restauranteId(), event.caminhoOriginal());
    }
}
