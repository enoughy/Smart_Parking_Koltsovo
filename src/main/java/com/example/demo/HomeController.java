package com.example.demo;


import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {
    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        model.addAttribute("lat", 54.84810431985595);
        model.addAttribute("lng", 83.09447706191598);
        model.addAttribute("zoom", 13);
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isLoggedIn", isLoggedIn);
        return "index";
    }
    @GetMapping("/register")
    public String register() {
        return "register"; // templates/register.html
    }

    // Переход на страницу входа
    @GetMapping("/login")
    public String login() {
        return "login"; // templates/login.html
    }
}