package com.example.yatra;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hotel {

    @GetMapping("/hotel")
    public String hotel() {
        return "Welcome to the Hotel pratik ";
    }
}
