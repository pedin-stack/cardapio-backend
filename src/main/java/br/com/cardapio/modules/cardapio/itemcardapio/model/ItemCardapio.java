package br.com.cardapio.modules.cardapio.itemcardapio.model;

import br.com.cardapio.infrastructure.model.PersistenceEntity;
import br.com.cardapio.modules.cardapio.categoria.model.Categoria;
import br.com.cardapio.modules.cardapio.itemcardapio.enums.StatusVideo;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Prato/bebida do cardápio: até 8 imagens e 1 vídeo (servido em HLS).
 */
@Entity
@Table(name = "itens_cardapio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ItemCardapio extends PersistenceEntity {

    public static final int MAXIMO_IMAGENS = 8;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "ordem", nullable = false)
    private int ordem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Restaurante restaurante;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "itens_categorias",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    @BatchSize(size = 50)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Categoria> categorias = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "itens_ingredientes", joinColumns = @JoinColumn(name = "item_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "nome", nullable = false, length = 80)
    @BatchSize(size = 50)
    private List<String> ingredientes = new ArrayList<>();

    /** Caminhos no storage, na ordem de exibição do carrossel. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "itens_imagens", joinColumns = @JoinColumn(name = "item_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "caminho", nullable = false, length = 500)
    @BatchSize(size = 50)
    private List<String> imagens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "video_status", nullable = false, length = 20)
    private StatusVideo videoStatus = StatusVideo.SEM_VIDEO;

    @Column(name = "video_original_path", length = 500)
    private String videoOriginalPath;

    @Column(name = "video_hls_path", length = 500)
    private String videoHlsPath;

    @Column(name = "video_poster_path", length = 500)
    private String videoPosterPath;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}
