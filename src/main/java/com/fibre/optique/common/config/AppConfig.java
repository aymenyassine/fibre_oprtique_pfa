package com.fibre.optique.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Global application configuration.
 *
 * <p>Provides the async thread pool used by {@code @Async} notification handlers.
 * Named "taskExecutor" so Spring picks it up as the default {@code @Async} executor.</p>
 */
@Configuration
public class AppConfig {

    @Bean(name = "taskExecutor")
    Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-notification-");
        executor.initialize();
        return executor;
    }
}
