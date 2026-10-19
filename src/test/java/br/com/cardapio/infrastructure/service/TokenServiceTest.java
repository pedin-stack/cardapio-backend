package br.com.cardapio.infrastructure.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private final TokenService service = new TokenService();

    {
        ReflectionTestUtils.setField(service, "secretKey", "segredo-de-teste");
        ReflectionTestUtils.setField(service, "expirationHours", 1L);
        service.init();
    }

    @Test
    @DisplayName("Token do cliente devolve o id do cliente")
    void validateTokenCliente_TokenDeCliente_DevolveId() {
        final UUID clienteId = UUID.randomUUID();

        assertThat(service.validateTokenCliente(service.generateTokenCliente(clienteId))).isEqualTo(clienteId);
    }

    @Test
    @DisplayName("Sessão do gestor, token ausente ou malformado não identificam cliente")
    void validateTokenCliente_TokenQueNaoEDeCliente_DevolveNulo() {
        final String sessaoGestor = service.generateToken("gestor@cardapio.dev", service.generateExpiration());

        assertThat(service.validateTokenCliente(sessaoGestor)).isNull();
        assertThat(service.validateTokenCliente(null)).isNull();
        assertThat(service.validateTokenCliente("nao-e-jwt")).isNull();
    }
}
