package com.tracker.expense.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration.
 * Swagger UI: http://localhost:8080/swagger-ui/index.html
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Paste your JWT token here (without 'Bearer' prefix)"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Expense Tracker API")
                        .version("1.0")
                        .description("APIs for managing expenses, income, lending, credit card tracking, budget, and analytics.")
                        .contact(new Contact()
                                .name("FinTrack Support")
                                .email("support@fintrack.app")))
                // Apply JWT security globally to all protected endpoints
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
