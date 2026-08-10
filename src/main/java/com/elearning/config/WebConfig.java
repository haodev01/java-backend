package com.elearning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class WebConfig {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        // Client gửi size lớn hơn 50 -> Spring tự CẮT về 50, không báo lỗi,
        // không throw exception — âm thầm bảo vệ, không làm vỡ request của client.
        return resolver -> resolver.setMaxPageSize(50);
    }
}