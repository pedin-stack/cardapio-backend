package br.com.cardapio.infrastructure.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Classe-base das entidades persistentes: identificador UUID gerado automaticamente.
 * <p>
 * Igualdade por identidade (como no blueprint, §4.1.10): compare entidades por {@code getId()}.
 * </p>
 */
@MappedSuperclass
public class PersistenceEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter
    private UUID id;
}
