package br.com.cardapio.modules.core.restaurante.listener;

import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoVideoService;
import br.com.cardapio.modules.core.midia.service.IMidiaService;
import br.com.cardapio.modules.core.restaurante.event.RestauranteExcluidoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Apaga do disco os vídeos do restaurante excluído, depois do commit.
 * As fotos no bucket do Supabase ficam (limpeza do bucket é manual por enquanto).
 */
@Component
@RequiredArgsConstructor
public class RestauranteExcluidoListener {

    private final IArmazenamentoVideoService armazenamentoVideoService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRestauranteExcluido(final RestauranteExcluidoEvent event) {
        armazenamentoVideoService.deleteDiretorio(IMidiaService.findPastaRestaurante(event.restauranteId()));
    }
}
