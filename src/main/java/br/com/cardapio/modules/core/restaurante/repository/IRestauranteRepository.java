package br.com.cardapio.modules.core.restaurante.repository;

import br.com.cardapio.modules.core.video.enums.StatusVideo;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório da entidade {@link Restaurante}.
 */
@Repository
public interface IRestauranteRepository extends JpaRepository<Restaurante, UUID> {

    /** Restaurante desativado (ou inexistente) responde vazio: o cardápio público vira "Serviço indisponível". */
    Optional<Restaurante> findBySlugAndAtivoTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    Page<Restaurante> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /**
     * Grava o resultado da transcodificação do vídeo da capa só se o original ainda for o mesmo
     * (o gestor pode ter trocado o vídeo enquanto o FFmpeg rodava).
     *
     * @return linhas afetadas (0 = vídeo trocado/restaurante excluído; resultado descartado)
     */
    @Transactional
    @Modifying
    @Query("UPDATE Restaurante r SET r.videoStatus = :status, r.videoHlsPath = :hlsPath, r.videoPosterPath = :posterPath "
            + "WHERE r.id = :id AND r.videoOriginalPath = :originalPath")
    int updateVideoResultado(@Param("id") UUID id,
                             @Param("originalPath") String originalPath,
                             @Param("status") StatusVideo status,
                             @Param("hlsPath") String hlsPath,
                             @Param("posterPath") String posterPath);
}
