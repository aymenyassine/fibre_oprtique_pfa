package com.fibre.optique.common.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.cfg.DateTimeFeature;

/**
 * Configuration Jackson globale.
 * <p>
 * Spring Boot 4 / Jackson 3 n'utilise plus {@code Jackson2ObjectMapperBuilderCustomizer}
 * ni les packages {@code com.fasterxml.jackson.*} pour le moteur de sérialisation —
 * remplacés par les packages {@code tools.jackson.*} de Jackson 3 et le callback
 * {@link JsonMapperBuilderCustomizer} (package {@code org.springframework.boot.jackson.autoconfigure}),
 * qui configure le nouveau {@code JsonMapper.Builder}.
 * </p>
 * <p>
 * Important : {@code SerializationFeature.WRITE_DATES_AS_TIMESTAMPS} (Jackson 2) a été
 * déplacé vers {@link DateTimeFeature#WRITE_DATES_AS_TIMESTAMPS} en Jackson 3, et est
 * désormais désactivé par défaut. Les dates sont donc déjà sérialisées en ISO-8601
 * sans configuration supplémentaire — ce bean ne fait que le rendre explicite.
 * </p>
 * <p>
 * Le module JSR-310 (java.time) n'a pas besoin d'être enregistré manuellement :
 * Spring Boot 4 détecte et enregistre automatiquement tous les modules Jackson
 * présents sur le classpath.
 * </p>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                // Comportement par défaut en Jackson 3 — explicite pour la clarté du code
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}