package com.example.yatra;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Pratik {

    @GetMapping("/pratik")
    public String pratik() { return "hi Welcome to the My cab service with pratik ";
    }
}
