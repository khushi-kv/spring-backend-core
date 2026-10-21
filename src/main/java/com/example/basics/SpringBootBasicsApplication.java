package com.example.basics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication is a combination of 3 annotations:
 * 1. @Configuration: Tags the class as a source of bean definitions.
 * 2. @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings.
 * 3. @ComponentScan: Tells Spring to scan the current package (com.example.basics) 
 *    and all sub-packages for Spring components (@Component, @Service, @Repository, @RestController, etc.).
 */
@SpringBootApplication
public class SpringBootBasicsApplication {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootBasicsApplication.class);

    public static void main(String[] args) {
        // Starts the Spring IoC Container and embedded Tomcat Web Server on port 8080
        SpringApplication.run(SpringBootBasicsApplication.class, args);
        logger.info("=======================================================");
        logger.info("   SPRING BOOT APPLICATION STARTED SUCCESSFULLY!    ");
        logger.info("=======================================================");
    }
}
