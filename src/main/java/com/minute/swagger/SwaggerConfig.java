package com.minute.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Minute API", version = "v1.0", description = "유튜브 여행 숏츠 큐레이팅 API 문서"),
        servers = @Server(url = "http://localhost:8080",description = "Local Sever")
)
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("com.minute.video.controller") // API Controller 패키지 위치
                .build();
    }
}
