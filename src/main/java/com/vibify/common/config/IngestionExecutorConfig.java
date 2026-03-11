package com.vibify.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class IngestionExecutorConfig {

    @Bean
    public ExecutorService ingestionExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}
