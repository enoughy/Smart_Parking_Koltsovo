package com.example.demo.handler;

import com.example.demo.controller.UserController;
import com.example.demo.entity.UserAlreadyExistsException;
import com.example.demo.entity.UserData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequestMapping("/api/auth")
public class UserHttpHandler {

    private final UserController userController;

    public UserHttpHandler(UserController userController) {
        this.userController = userController;
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request, Model model) {
        try {
            userController.register(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword()
            );
            return "redirect:/login";

        }catch (UserAlreadyExistsException e) {
            model.addAttribute("error", e.getMessage());
            return "register"; // ВАЖНО: не redirect
        }

    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request, Model model, HttpServletRequest httpRequest) {
        try {
            Authentication auth = userController.login(
                    request.getEmail(),
                    request.getPassword()
            );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            httpRequest.getSession(true)
                    .setAttribute(
                            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                            context
                    );
            return "redirect:/";

        } catch (BadCredentialsException e) {
            model.addAttribute("error", "Неверный email или пароль");
            return "login"; // показываем страницу логина с ошибкой
        } catch (DisabledException e) {
            model.addAttribute("error", "Аккаунт отключен");
            return "login";
        } catch (LockedException e) {
            model.addAttribute("error", "Аккаунт заблокирован");
            return "login";
        }
    }
}
