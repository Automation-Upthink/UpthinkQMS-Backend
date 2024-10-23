//package com.upthink.qms.config;
//
//import io.github.cdimascio.dotenv.Dotenv;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.env.EnvironmentPostProcessor;
//import org.springframework.core.env.ConfigurableEnvironment;
//import org.springframework.core.env.MapPropertySource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {
//
//    private static final String PROPERTY_SOURCE_NAME = "dotenv";
//    private static final Logger logger = LoggerFactory.getLogger(DotenvEnvironmentPostProcessor.class);
//
//    @Override
//    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
//        // Determine which .env file to load based on an environment variable or a Spring profile
//        String activeProfile = environment.getProperty("spring.profiles.active", "default");
//        String dotenvFileName = ".env.local";
//
////        if ("production".equalsIgnoreCase(activeProfile)) {
////            dotenvFileName = ".env.local";
////        } else if ("development".equalsIgnoreCase(activeProfile)) {
////            dotenvFileName = ".env.development";
////        }
//        // Add more profiles as needed
//
//        logger.info("Loading environment variables from {}", dotenvFileName);
//
//        // Load the specified .env file
//        Dotenv dotenv = Dotenv.configure()
//                .filename(dotenvFileName)
//                .ignoreIfMissing()
//                .load();
//
//        // Convert Dotenv variables to a Map
//        Map<String, Object> dotenvMap = new HashMap<>();
//        dotenv.entries().forEach(entry -> dotenvMap.put(entry.getKey(), entry.getValue()));
//
//        // Add the Dotenv variables as a new PropertySource
//        MapPropertySource dotenvPropertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, dotenvMap);
//        environment.getPropertySources().addFirst(dotenvPropertySource);
//
//        logger.info("Environment variables from {} have been added to the Spring Environment.", dotenvFileName);
//    }
//}
