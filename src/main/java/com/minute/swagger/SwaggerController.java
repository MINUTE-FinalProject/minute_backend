package com.minute.swagger;

import org.springframework.web.bind.annotation.*;

@RestController
public class SwaggerController {

    // Swagger UI 페이지를 위한 GET 요청
    @GetMapping("/swagger-ui")
    public String swaggerUI() {
        return "Swagger UI 페이지로 이동해주세요.";
    }

}