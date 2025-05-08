package com.example.demo.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI storageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Storage API")
                        .description("API for managing PDF storage operations")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@example.com")));
    }
}