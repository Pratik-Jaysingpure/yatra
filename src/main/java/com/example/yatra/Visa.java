package com.example.yatra;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Visa {

    @GetMapping("/visa")
    public String visa() {
        return "Welcome to the My Visa please book ";
    }
}