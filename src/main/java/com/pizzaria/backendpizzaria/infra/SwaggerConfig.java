package com.pizzaria.backendpizzaria.infra;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${app.public-api-base-url:}") String publicApiBaseUrl) {
        final String securitySchemeName = "bearerAuth";
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("API Napolitech - Documentação Oficial")
                        .description("Esta API é parte do projeto Napolitech, desenvolvido com Spring Boot e MySQL. Aqui você encontrará detalhes dos endpoints e exemplos de uso.")
                        .version("1.0.1")
                        .contact(new Contact()
                                .name("Equipe Napolitech")
                                .url("https://github.com/NapoliTech")
                                .email("napolitech.dev@gmail.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                        .components(new Components()
                                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
        if (StringUtils.hasText(publicApiBaseUrl)) {
            openAPI.addServersItem(new Server().url(publicApiBaseUrl));
        }
        return openAPI;
    }
}
