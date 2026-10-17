package br.com.cardapio.modules.core.perfil.model;

import br.com.cardapio.infrastructure.model.PersistenceEntity;
import br.com.cardapio.modules.core.perfil.enums.Modulo;
import br.com.cardapio.modules.core.perfil.enums.Permissao;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Pacote nomeado de permissões. Os perfis são fixos (criados por migration com UUID fixo):
 * {@value #SUPER_ADMINISTRADOR} e {@value #GESTOR_RESTAURANTE}.
 */
@Entity
@Table(name = "perfis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Perfil extends PersistenceEntity {

    public static final String SUPER_ADMINISTRADOR = "Super Administrador";
    public static final String GESTOR_RESTAURANTE = "Gestor Restaurante";

    @Column(name = "nome", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "modulo", nullable = false, length = 50)
    private Modulo modulo;

    @Column(name = "imutavel", nullable = false)
    private boolean imutavel = true;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "perfil_permissoes", joinColumns = @JoinColumn(name = "perfil_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permissao", nullable = false, length = 100)
    private Set<Permissao> permissoes = new HashSet<>();
}
