package br.com.cardapio.config.cache;

import br.com.cardapio.config.security.dto.UsuarioUserDetailsDTO;
import br.com.cardapio.modules.core.usuario.enums.StatusUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RedisCacheConfigTest {

    @Test
    @DisplayName("O principal do JWT sobrevive ida e volta pelo serializador do cache userDetails")
    void serializador_UsuarioUserDetailsDTO_IdaEVolta() {
        final RedisCacheConfiguration config = new RedisCacheConfig().redisCacheConfiguration();
        final RedisSerializationContext.SerializationPair<Object> par = config.getValueSerializationPair();
        final UsuarioUserDetailsDTO original = new UsuarioUserDetailsDTO(UUID.randomUUID(), "Gestor", "g@x.com", "hash",
                StatusUsuario.ATIVO, "Gestor Restaurante", UUID.randomUUID(), true, Set.of("CARDAPIO_ITEM_CRIAR"));

        final ByteBuffer bytes = par.write(original);
        final Object lido = par.read(bytes);

        assertThat(lido).isEqualTo(original);
    }
}
