package com.upthink.qms.config;

//import com.upthink.qms.service.AuthenticatedRequestArgumentResolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.upthink.qms.service.TokenValidationArgumentResolver;
import com.upthink.qms.service.TokenValidationArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // Register the AuthenticatedRequestArgumentResolver
        System.out.println("Registering TokenValidationArgumentResolver");
        resolvers.add(new TokenValidationArgumentResolver(gson()));
        System.out.println("Resolvers " + resolvers);
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder().create();
    }
}
