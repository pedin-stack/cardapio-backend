package br.com.cardapio.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendOrigemFilterTest {

    private static final String SEGREDO = "segredo-compartilhado-123";

    private MockHttpServletResponse executar(final FrontendOrigemFilter filtro, final String caminho, final String header) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", caminho);
        request.setServletPath(caminho);
        if (header != null) {
            request.addHeader(FrontendOrigemFilter.HEADER, header);
        }
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();
        filtro.doFilter(request, response, chain);
        // A cadeia só segue (request repassado) quando o filtro deixa passar.
        if (chain.getRequest() == null && response.getStatus() == 200) {
            response.setStatus(-1);
        }
        return response;
    }

    @Test
    @DisplayName("Com o segredo do front, /api segue a cadeia")
    void api_ComSegredo_Passa() throws Exception {
        assertThat(executar(new FrontendOrigemFilter(SEGREDO), "/api/v1/cardapio/itens", SEGREDO).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Sem o header ou com segredo errado, /api responde 403")
    void api_SemSegredoOuErrado_403() throws Exception {
        final FrontendOrigemFilter filtro = new FrontendOrigemFilter(SEGREDO);
        assertThat(executar(filtro, "/api/v1/autenticacao/sessoes/sessao", null).getStatus()).isEqualTo(403);
        assertThat(executar(filtro, "/api/v1/autenticacao/sessoes/sessao", "segredo-compartilhado-12").getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("/videos fica público: o player do navegador baixa os chunks direto da VPS")
    void videos_SemSegredo_Passa() throws Exception {
        assertThat(executar(new FrontendOrigemFilter(SEGREDO), "/videos/restaurantes/x/master.m3u8", null).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Segredo vazio desliga o filtro (dev)")
    void segredoVazio_Desligado() throws Exception {
        assertThat(executar(new FrontendOrigemFilter(""), "/api/v1/cardapio/itens", null).getStatus()).isEqualTo(200);
    }
}
