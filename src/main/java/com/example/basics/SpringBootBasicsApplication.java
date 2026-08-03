package com.example.basics;

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

    public static void main(String[] args) {
        // Starts the Spring IoC Container and embedded Tomcat Web Server on port 8080
        SpringApplication.run(SpringBootBasicsApplication.class, args);
        System.out.println("\n=======================================================");
        System.out.println("   SPRING BOOT APPLICATION STARTED SUCCESSFULLY!    ");
        System.out.println("=======================================================\n");
    }
}
