package br.com.cardapio.modules.cardapio.cliente.model;

import br.com.cardapio.infrastructure.model.PersistenceEntity;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Cliente que se identificou no cardápio público de um restaurante (para comentar e avaliar). Um cadastro por CPF
 * em cada restaurante; sem senha nem verificação por SMS (a identificação é declarada).
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cliente extends PersistenceEntity {

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    /** DDD + número, só dígitos. */
    @Column(name = "telefone", nullable = false, length = 11)
    private String telefone;

    /** Só dígitos. Nunca sai nas respostas públicas. */
    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Restaurante restaurante;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}
