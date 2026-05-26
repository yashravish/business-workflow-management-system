// Translated from: backend/app/main.py (create_app / app entry point)
package com.example.bwms;

import com.example.bwms.core.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
@EnableConfigurationProperties(AppConfig.class)
public class BwmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BwmsApplication.class, args);
    }

    // Python: GET /health and GET / meta endpoints
    @RestController
    static class MetaController {

        @GetMapping("/health")
        public Map<String, String> health() {
            return Map.of("status", "ok");
        }

        @GetMapping("/")
        public Map<String, String> root() {
            return Map.of(
                    "name", "Business Workflow Management System",
                    "docs", "/swagger-ui.html",
                    "health", "/health"
            );
        }
    }
}
