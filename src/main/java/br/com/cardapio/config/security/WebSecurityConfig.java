package br.com.cardapio.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração central de segurança: stateless, filtros RateLimit → JWT, rotas públicas explícitas e
 * RBAC fino por {@code @PreAuthorize} nos controllers.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    /** Rota pública do cardápio de um restaurante e seus sub-recursos (curtida, visualizações). */
    private static final String CARDAPIO_PUBLICO = "/api/v1/cardapio/restaurantes/restaurante/*";

    private final FrontendOrigemFilter frontendOrigemFilter;
    private final JWTLoginFilter jwtLoginFilter;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sem sessão o padrão seria 403 para anônimo; 401 permite ao front distinguir "deslogado".
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(authorize -> authorize
                        // Documentação da API
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Pré-voo do CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Login/logout: feitos por quem ainda não tem (ou acabou de perder) a sessão
                        .requestMatchers(HttpMethod.POST, "/api/v1/autenticacao/sessoes/sessao").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/autenticacao/sessoes/sessao").permitAll()
                        // Cardápio público: o cliente do restaurante não tem conta
                        .requestMatchers(HttpMethod.GET, CARDAPIO_PUBLICO).permitAll()
                        // Curtir/descurtir e métricas de tempo: anônimos, identificados só pelo dispositivoId
                        .requestMatchers(HttpMethod.PUT, CARDAPIO_PUBLICO + "/itens/item/*/curtida").permitAll()
                        .requestMatchers(HttpMethod.DELETE, CARDAPIO_PUBLICO + "/itens/item/*/curtida").permitAll()
                        .requestMatchers(HttpMethod.POST, CARDAPIO_PUBLICO + "/visualizacoes").permitAll()
                        // Chunks HLS e pôsteres do feed público (servidos do disco pelo WebMvcConfig)
                        .requestMatchers(HttpMethod.GET, "/videos/**").permitAll()
                        .anyRequest().authenticated())
                // Ordem: 1º só o front passa → 2º rate limit → 3º JWT.
                .addFilterBefore(frontendOrigemFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtLoginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
