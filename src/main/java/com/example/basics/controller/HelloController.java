package com.example.basics.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * START HERE FOR BEGINNERS!
 * 
 * @RestController tells Spring Boot that this class handles HTTP requests 
 * and returns the response directly as text or JSON to the browser.
 */
@RestController
public class HelloController {

    // Step 1: Basic GET request -> Open http://localhost:8080/hello in your browser
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello! Welcome to Spring Boot!";
    }

    // Step 2: GET request with query parameter -> http://localhost:8080/greet?name=Khush
    @GetMapping("/greet")
    public String greetUser(@RequestParam(defaultValue = "Learner") String name) {
        return "Hello " + name + "! You are ready to learn Spring Boot.";
    }
}
