package com.bookread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BookreadApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookreadApplication.class, args);
	}
    @GetMapping
    public String helloWorld() {
        return "Hello World Spring Boot!";
    }

}
