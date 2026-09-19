package com.internship.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger/OpenAPI metadata. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(new Info()
                .title("Internship & Campus Hiring API")
                .version("1.0.0")
                .description("Capstone: student/company/admin hiring workflows"));
    }
}
