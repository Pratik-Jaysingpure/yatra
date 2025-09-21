package com.example.yatra;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Bus {

    @GetMapping("/bus")
    public String bus() {
        return "Welcome to the My buses  ";
    }
}
