package com.example.yatra;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Holiday {

    @GetMapping("/holiday")
    public String holiday() {
        return "using mvn clean package this is is build check hi pratik add the pool scm  ";
    }
}
