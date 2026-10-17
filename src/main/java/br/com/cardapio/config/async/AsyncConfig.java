package br.com.cardapio.config.async;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executores assíncronos: o genérico ({@code taskExecutor}) e um dedicado à transcodificação de vídeo.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /** Pool genérico (10/50/500, CallerRunsPolicy: contrapressão sem perder tarefa). */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Pool do FFmpeg. Transcodificar é CPU-bound: poucas threads fixas ({@code app.video.threads}, padrão 2) para não
     * competir com o Tomcat — numa VPS compartilhada use 1; os demais vídeos esperam na fila (PROCESSANDO no painel).
     */
    @Bean(name = "videoTaskExecutor")
    public Executor videoTaskExecutor(@Value("${app.video.threads}") final int threads) {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threads);
        executor.setMaxPoolSize(threads);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("VideoThread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }
}
