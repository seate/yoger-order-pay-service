package com.project.yogerOrder.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private Info apiInfo() {
        return new Info()
                .title("yoger-order") // API의 제목
                .description("API docs about yoger-order") // API에 대한 설명
                .version("1.0.0"); // API의 버전
    }

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("Bearer Token", securityScheme))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .info(apiInfo());
    }
}
