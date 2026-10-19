package br.com.cardapio.modules.cardapio.itemcardapio.repository;

import br.com.cardapio.modules.cardapio.itemcardapio.enums.StatusVideo;
import br.com.cardapio.modules.cardapio.itemcardapio.model.ItemCardapio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repositório da entidade {@link ItemCardapio}. As coleções (categorias, ingredientes, imagens) carregam em lote
 * ({@code @BatchSize}), sem N+1 nas listagens.
 */
@Repository
public interface IItemCardapioRepository extends JpaRepository<ItemCardapio, UUID> {

    List<ItemCardapio> findByRestauranteIdOrderByOrdemAscNomeAsc(UUID restauranteId);

    List<ItemCardapio> findByRestauranteIdAndAtivoTrueOrderByOrdemAscNomeAsc(UUID restauranteId);

    Page<ItemCardapio> findByRestauranteId(UUID restauranteId, Pageable pageable);

    /** Itens em qualquer uma das categorias; {@code Distinct}: o item em duas delas aparece uma vez só. */
    Page<ItemCardapio> findDistinctByRestauranteIdAndCategoriasIdIn(UUID restauranteId, Collection<UUID> categoriaIds, Pageable pageable);

    Optional<ItemCardapio> findByIdAndRestauranteId(UUID id, UUID restauranteId);

    boolean existsByIdAndRestauranteIdAndAtivoTrue(UUID id, UUID restauranteId);

    @Query("SELECT i.id FROM ItemCardapio i WHERE i.restaurante.id = :restauranteId AND i.id IN :ids")
    Set<UUID> findIdsByRestauranteIdAndIdIn(@Param("restauranteId") UUID restauranteId, @Param("ids") Set<UUID> ids);

    /**
     * Grava o resultado da transcodificação só se o vídeo do item ainda for o mesmo original
     * (o gestor pode ter trocado o vídeo enquanto o FFmpeg rodava).
     *
     * @return linhas afetadas (0 = vídeo trocado/item excluído; resultado descartado)
     */
    @Transactional
    @Modifying
    @Query("UPDATE ItemCardapio i SET i.videoStatus = :status, i.videoHlsPath = :hlsPath, i.videoPosterPath = :posterPath "
            + "WHERE i.id = :id AND i.videoOriginalPath = :originalPath")
    int updateVideoResultado(@Param("id") UUID id,
                             @Param("originalPath") String originalPath,
                             @Param("status") StatusVideo status,
                             @Param("hlsPath") String hlsPath,
                             @Param("posterPath") String posterPath);
}
