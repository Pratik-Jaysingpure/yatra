package com.example.yatra;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Flight {

    @GetMapping("/flight")
    public String flight() {
        return " pratik Welcome to the My Flight";
    }
}
