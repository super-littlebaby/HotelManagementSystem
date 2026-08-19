package com.project.hotelmanagementsystem.config;

import com.project.hotelmanagementsystem.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/employees/login",
                        "/api/guests/login",
                        "/api/guests/register",
                        "/api/room-types/search/byHotelId",
                        "/api/rooms/search/byHotelId",
                        "/api/facilities",
                        "/api/reservations/create",
                        "/api/reservations/search/**",
                        "/api/reservations/my/**",
                        "/api/reservations/*/cancel"
                );
    }
}
