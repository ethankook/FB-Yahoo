package com.example.fbyahoo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {"/", "/login", "/leagues", "/leagues/{path:[\\w.]+}"})
    public String forward() {
        return "forward:/index.html";
    }
}
