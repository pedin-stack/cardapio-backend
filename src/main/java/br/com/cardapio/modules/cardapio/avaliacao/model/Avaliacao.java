package br.com.cardapio.modules.cardapio.avaliacao.model;

import br.com.cardapio.infrastructure.model.PersistenceEntity;
import br.com.cardapio.modules.cardapio.cliente.model.Cliente;
import br.com.cardapio.modules.cardapio.itemcardapio.model.ItemCardapio;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Estrelas (1 a 5) que um cliente identificado deu a um item. Uma por cliente em cada item: avaliar de novo troca
 * as estrelas (índice único na migration).
 */
@Entity
@Table(name = "avaliacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Avaliacao extends PersistenceEntity {

    /** {@code short}: a coluna é SMALLINT (o ddl-auto=validate recusaria um int). */
    @Column(name = "estrelas", nullable = false)
    private short estrelas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ItemCardapio item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cliente cliente;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}
