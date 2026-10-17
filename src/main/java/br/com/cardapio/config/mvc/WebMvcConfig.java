package br.com.cardapio.config.mvc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Serve o HLS gravado em disco em {@code /videos/**} (com suporte a Range).
 * Cada envio de vídeo tem pasta com UUID próprio: os arquivos nunca mudam, então o cache é de 1 ano.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.video.diretorio}")
    private String diretorioVideos;

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        // O Spring exige a barra final na localização (o toUri só a põe se a pasta já existir).
        final String local = Path.of(diretorioVideos).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/videos/**")
                .addResourceLocations(local.endsWith("/") ? local : local + "/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
    }
}
