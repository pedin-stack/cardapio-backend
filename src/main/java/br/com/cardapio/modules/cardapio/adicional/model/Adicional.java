package br.com.cardapio.modules.cardapio.adicional.model;

import br.com.cardapio.infrastructure.model.PersistenceEntity;
import br.com.cardapio.modules.cardapio.itemcardapio.model.ItemCardapio;
import br.com.cardapio.modules.core.restaurante.model.Restaurante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Adicional pago que o cliente pode somar ao item ("mais carne", "molho extra"). Diferente da especificação
 * (escolha sem custo), tem valor. Um adicional vale para vários itens.
 */
@Entity
@Table(name = "adicionais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Adicional extends PersistenceEntity {

    @Column(name = "nome", nullable = false, length = 60)
    private String nome;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Restaurante restaurante;

    /** Lista (bag), não Set: um Set chamaria o hashCode do item, que toca as coleções lazy dele. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "itens_adicionais",
            joinColumns = @JoinColumn(name = "adicional_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    @BatchSize(size = 50)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ItemCardapio> itens = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
