package com.motorcardgame.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadata del spec OpenAPI que expone springdoc en {@code /v3/api-docs} y Swagger UI en
 * {@code /swagger-ui.html}. No añade seguridad ni agrupa endpoints — springdoc ya documenta todos
 * los {@code @RestController} del classpath por defecto.
 */
@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI motorCardGameOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MotorCardGame API")
                        .description("Motor genérico de juegos de cartas: GameDefinition, GameDefinitionVersion, "
                                + "GameInstance y PlayerAction.")
                        .version("0.1.0"));
    }
}
