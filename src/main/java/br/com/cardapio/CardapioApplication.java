package br.com.cardapio;

import br.com.cardapio.config.time.SpringTimezoneConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Ponto de entrada da API do Cardápio.
 */
@SpringBootApplication
@EnableCaching
public class CardapioApplication {

    public static void main(String[] args) {
        // Antes do Spring: o relógio do Hibernate nasce com o fuso padrão do momento (ver SpringTimezoneConfig).
        SpringTimezoneConfig.aplicarFusoPadrao();
        SpringApplication.run(CardapioApplication.class, args);
    }

}
