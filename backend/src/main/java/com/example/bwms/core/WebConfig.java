// Registers String→Enum converters for @RequestParam so "pending" → TaskStatus.PENDING works
package com.example.bwms.core;

import com.example.bwms.model.AuditAction;
import com.example.bwms.model.TaskPriority;
import com.example.bwms.model.TaskStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, TaskStatus.class, TaskStatus::fromValue);
        registry.addConverter(String.class, TaskPriority.class, TaskPriority::fromValue);
        registry.addConverter(String.class, AuditAction.class, AuditAction::fromValue);
    }
}
