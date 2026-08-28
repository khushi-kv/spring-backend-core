package com.example.basics.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configuration class for OpenAPI 3 / Swagger UI documentation.
 * Provides interactive API testing interface at /swagger-ui.html with Light/Dark Theme Switcher.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuthentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Production-Grade Spring Boot REST API")
                        .description("Production-ready backend API featuring REST Endpoints, PostgreSQL Support, " +
                                     "Bean Validation, Global Exception Handling, and OpenAPI 3 Swagger UI.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Khushi - Backend Engineering")
                                .email("khushi@example.com"))
                        .license(new License().name("Apache 2.0").url("https://spring.io")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /**
     * Injects custom CSS (swagger-theme.css) and JS (theme-toggle.js) directly into Swagger UI HTML head.
     */
    @Bean
    public SwaggerIndexTransformer swaggerIndexTransformer(
            SwaggerUiConfigProperties swaggerUiConfigProperties,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerUiConfigParameters swaggerUiConfigParameters,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider) {
        return new SwaggerIndexPageTransformer(
                swaggerUiConfigProperties,
                swaggerUiOAuthProperties,
                swaggerUiConfigParameters,
                swaggerWelcomeCommon,
                objectMapperProvider) {
            @Override
            public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain) throws IOException {
                if (resource.getFilename() != null && resource.getFilename().equals("index.html")) {
                    String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    String customHeadTags = "<link rel=\"stylesheet\" type=\"text/css\" href=\"/custom-swagger/swagger-theme.css\" />" +
                                            "<script src=\"/custom-swagger/theme-toggle.js\"></script>";
                    html = html.replace("</head>", customHeadTags + "</head>");
                    return new TransformedResource(resource, html.getBytes(StandardCharsets.UTF_8));
                }
                return super.transform(request, resource, transformerChain);
            }
        };
    }
}
