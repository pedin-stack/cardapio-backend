package br.com.cardapio.config.time;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Fixa o fuso padrão em America/Sao_Paulo: as métricas de horário/dia da semana das curtidas
 * dependem do horário local do restaurante (o container roda em UTC).
 */
@Configuration
public class SpringTimezoneConfig {

    @PostConstruct
    public void timezoneConfig() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }
}
