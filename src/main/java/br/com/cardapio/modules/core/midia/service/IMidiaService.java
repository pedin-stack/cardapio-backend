package br.com.cardapio.modules.core.midia.service;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.infrastructure.exception.BusinessException;
import br.com.cardapio.modules.core.midia.dto.request.UploadAssinadoRequestDTO;
import br.com.cardapio.modules.core.midia.dto.response.UploadAssinadoResponseDTO;

import java.util.UUID;

/**
 * Contrato da emissão de uploads assinados.
 */
public interface IMidiaService {

    /** Pasta do super admin (logo e capa dos restaurantes). */
    String PASTA_PLATAFORMA = "plataforma/";

    /** Pasta do gestor: tudo que ele envia fica abaixo dela (validado ao salvar itens). */
    static String findPastaRestaurante(final UUID restauranteId) {
        return "restaurantes/" + restauranteId + "/";
    }

    /**
     * Gera um caminho único (o nome do arquivo do cliente é descartado) e o token de upload.
     *
     * @param dto           tipo do arquivo
     * @param usuarioLogado define a pasta: a do restaurante (gestor) ou a da plataforma (super admin)
     * @return caminho, token e URL pública
     * @throws BusinessException {@code TIPO_ARQUIVO_NAO_SUPORTADO} (400) ou {@code STORAGE_FAILED} (500)
     */
    UploadAssinadoResponseDTO saveUploadAssinado(UploadAssinadoRequestDTO dto, UsuarioUserDetailsDTO usuarioLogado);
}
