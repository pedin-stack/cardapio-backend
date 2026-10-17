package br.com.cardapio.modules.core.restaurante.model;

import br.com.cardapio.infrastructure.model.PersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Restaurante cliente da plataforma. Desativado, o cardápio público responde "Serviço indisponível" (404)
 * e o gestor perde o acesso ao painel.
 * <p>
 * Mora no Core (e não no módulo cardápio) porque usuários e cardápio dependem dele (blueprint §2.2.2).
 * </p>
 */
@Entity
@Table(name = "restaurantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Restaurante extends PersistenceEntity {

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    /** Endereço público do cardápio: {@code /{slug}} no front. */
    @Column(name = "slug", nullable = false, unique = true, length = 80)
    private String slug;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @Column(name = "logo_path", length = 500)
    private String logoPath;

    @Column(name = "capa_path", length = 500)
    private String capaPath;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
