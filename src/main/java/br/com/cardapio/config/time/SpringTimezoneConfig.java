package br.com.cardapio.config.time;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Fixa o fuso padrão em America/Sao_Paulo (o container roda em UTC): toda data gravada ({@code LocalDateTime}) e as
 * métricas de horário/dia da semana das curtidas ficam no horário local do restaurante.
 * <p>
 * Tem que valer antes do Hibernate subir: o relógio do {@code @CreationTimestamp}/{@code @UpdateTimestamp} guarda o
 * fuso do momento em que é criado (aplicado só aqui, no {@code @PostConstruct}, ele gravava em UTC). Por isso o
 * {@code main} chama {@link #aplicarFusoPadrao()} antes do Spring; o {@code @PostConstruct} fica para os testes
 * ({@code @SpringBootTest} não passa pelo {@code main}).
 * </p>
 */
@Configuration
public class SpringTimezoneConfig {

    public static final String FUSO_PADRAO = "America/Sao_Paulo";

    public static void aplicarFusoPadrao() {
        TimeZone.setDefault(TimeZone.getTimeZone(FUSO_PADRAO));
    }

    @PostConstruct
    public void timezoneConfig() {
        aplicarFusoPadrao();
    }
}
