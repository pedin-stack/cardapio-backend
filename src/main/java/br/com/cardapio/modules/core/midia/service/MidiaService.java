package br.com.cardapio.modules.core.midia.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.armazenamento.IArmazenamentoArquivoService;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.infrastructure.exception.BusinessExceptionMessage;
import br.com.cardapio.modules.core.midia.dto.request.UploadAssinadoRequestDTO;
import br.com.cardapio.modules.core.midia.dto.response.UploadAssinadoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Emite uploads assinados com caminho gerado no servidor.
 * <p>
 * O tipo declarado é contrato de UX (o cliente pode mentir); a defesa real são os limites/MIME do bucket.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class MidiaService implements IMidiaService {

    /** MIME aceito → extensão gravada. */
    private static final Map<String, String> EXTENSOES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "video/mp4", "mp4",
            "video/quicktime", "mov",
            "video/webm", "webm");

    private final IArmazenamentoArquivoService armazenamentoArquivoService;

    @Override
    public UploadAssinadoResponseDTO saveUploadAssinado(final UploadAssinadoRequestDTO dto, final UsuarioUserDetailsDTO usuarioLogado) {
        final String tipo = dto.tipoConteudo().trim().toLowerCase();
        final String extensao = EXTENSOES.get(tipo);
        if (extensao == null) {
            throw new BusinessException(BusinessExceptionMessage.TIPO_ARQUIVO_NAO_SUPORTADO.getMessage());
        }

        final String pasta = usuarioLogado.restauranteId() != null
                ? IMidiaService.findPastaRestaurante(usuarioLogado.restauranteId())
                : PASTA_PLATAFORMA;
        final String subpasta = tipo.startsWith("video/") ? "videos/" : "imagens/";
        final String caminho = pasta + subpasta + UUID.randomUUID() + "." + extensao;

        final String token = armazenamentoArquivoService.createUploadToken(caminho);
        return new UploadAssinadoResponseDTO(caminho, token);
    }
}
