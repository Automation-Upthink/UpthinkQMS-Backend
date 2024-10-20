package com.upthink.qms.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.gson.Gson;
import com.upthink.qms.service.request.AuthenticatedRequest;
import com.upthink.qms.service.request.CreateEssayClientCredRequest;
import com.upthink.qms.service.request.TokenValidation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.util.stream.Collectors;


public class TokenValidationArgumentResolver implements HandlerMethodArgumentResolver {

    private final Gson gson;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Check if the controller parameter is of type AuthenticatedRequest or its subclass
        return AuthenticatedRequest.class.isAssignableFrom(parameter.getParameterType());
    }

    public TokenValidationArgumentResolver(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        // Get the request object
        HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        // Deserialize the JSON request body into the respective class
        String jsonBody = nativeRequest.getReader().lines().collect(Collectors.joining());
        ObjectMapper objectMapper = new ObjectMapper();

        // Use the parameter type to dynamically deserialize the appropriate subclass (e.g., CreateEssayClientCredRequest)
        AuthenticatedRequest request = (AuthenticatedRequest) gson.fromJson(jsonBody, parameter.getParameterType());
        // Extract the authentication object from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getCredentials() instanceof TokenValidation) {
            // Cast the credentials to TokenValidation
            TokenValidation tokenValidation = (TokenValidation) authentication.getCredentials();
            // Inject the TokenValidation object into the request (inherited field from AuthenticatedRequest)
            request.setId(tokenValidation);
        } else {
            System.out.println("Token validation not found in the authentication credentials");
        }

        return request;
    }
}
