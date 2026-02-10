package com.mptourism.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mptourism.model.AuthResponse;
import com.mptourism.model.Category;
import com.mptourism.model.Location;
import com.mptourism.model.LocationUpdateRequest;
import com.mptourism.model.LoginRequest;
import com.mptourism.model.RegisterRequest;
import com.mptourism.model.User;
import com.mptourism.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// THESE TWO ARE PUBLIC API'S BUT USED JSON WEB TOKEN
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // REGISTER USER
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return "User registered successfully";
    }

    // LOGIN USER
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return new AuthResponse(token);
    }
}
