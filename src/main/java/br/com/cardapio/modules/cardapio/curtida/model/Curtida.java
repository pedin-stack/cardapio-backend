package br.com.cardapio.modules.cardapio.curtida.model;

import br.com.cardapio.infrastructure.model.PersistenceEntity;
import br.com.cardapio.modules.cardapio.itemcardapio.model.ItemCardapio;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Curtida de um dispositivo anônimo num item. A curtida é o "pedido" do cardápio (aba Curtidos = carrinho).
 * <p>
 * Descurtir não apaga: preenche {@code removidoEm}. Curtida com {@code removidoEm} é um "quase pedido".
 * No máximo uma curtida ativa por (item, dispositivo) — índice único parcial na migration.
 * </p>
 */
@Entity
@Table(name = "curtidas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Curtida extends PersistenceEntity {

    @Column(name = "dispositivo_id", nullable = false)
    private UUID dispositivoId;

    @Column(name = "curtido_em", nullable = false)
    private LocalDateTime curtidoEm;

    @Column(name = "removido_em")
    private LocalDateTime removidoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Restaurante restaurante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ItemCardapio item;
}
