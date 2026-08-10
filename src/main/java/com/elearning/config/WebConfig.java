package com.elearning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig  implements WebMvcConfigurer {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        // Client gửi size lớn hơn 50 -> Spring tự CẮT về 50, không báo lỗi,
        // không throw exception — âm thầm bảo vệ, không làm vỡ request của client.
        return resolver -> resolver.setMaxPageSize(50);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ URL /uploads/** sang thư mục vật lý ./uploads/ — file lưu ở
        // Bước 3 giờ truy cập được qua HTTP, vd http://localhost:8080/uploads/thumbnails/xxx.png
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}