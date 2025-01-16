package com.project.yogerOrder.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
@RequiredArgsConstructor
public class GlobalConfig {

    private final InjectedGlobalConfig config;


    @PostConstruct
    public void started() {
        TimeZone.setDefault(TimeZone.getTimeZone(config.timeZone));
    }


    @ConfigurationProperties(prefix = "global")
    public record InjectedGlobalConfig(@NotBlank String timeZone) {
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
