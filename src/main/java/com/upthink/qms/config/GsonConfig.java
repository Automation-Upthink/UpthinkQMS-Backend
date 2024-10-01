package com.upthink.qms.config;

//import com.google.gson.Gson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gson.CustomGsonBuilder;
import gson.GsonDTO;
import gson.GsonObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

@Configuration
public class GsonConfig {


    @Bean
    public HttpMessageConverter<Object> gsonHttpMessageConverter(Gson gson) {
        GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        gsonConverter.setGson(gson);
        return gsonConverter;
    }
}
