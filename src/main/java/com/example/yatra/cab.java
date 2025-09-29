package com.example.yatra;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class cab {

    @GetMapping("/cab")
    public String Cab() { return " pratik hi Welcome to the My cab service ";
    }
}
