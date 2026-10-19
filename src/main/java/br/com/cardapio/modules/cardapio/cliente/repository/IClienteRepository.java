package br.com.cardapio.modules.cardapio.cliente.repository;

import br.com.cardapio.modules.cardapio.cliente.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório da entidade {@link Cliente}. Toda consulta recebe o restaurante (o mesmo CPF é outro cliente em outro
 * restaurante).
 */
@Repository
public interface IClienteRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByRestauranteIdAndCpf(UUID restauranteId, String cpf);

    Optional<Cliente> findByIdAndRestauranteId(UUID id, UUID restauranteId);
}
