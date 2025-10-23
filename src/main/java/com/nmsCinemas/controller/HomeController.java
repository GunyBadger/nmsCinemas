package com.nmsCinemas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/home", "/ui"})
    public String home() {
        return "index"; // templates/index.html
    }
}