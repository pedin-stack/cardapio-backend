package br.com.cardapio.modules.cardapio.comentario.model;

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

import java.time.LocalDateTime;

/**
 * Comentário público de um cliente identificado num item. O gestor pode apagar (moderação).
 */
@Entity
@Table(name = "comentarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Comentario extends PersistenceEntity {

    @Column(name = "texto", nullable = false, length = 500)
    private String texto;

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

    /**
     * Preenchido pelo service com {@code LocalDateTime.now()} (horário de Brasília, como as curtidas): o relógio do
     * {@code @CreationTimestamp} nasce antes do {@code SpringTimezoneConfig} e gravaria em UTC — e a hora aparece na tela.
     */
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
