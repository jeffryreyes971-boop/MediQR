package com.mediqr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio() {
        return "redirect:/dashboard";   // si no hay sesión, Spring Security lo manda al login
    }
}
