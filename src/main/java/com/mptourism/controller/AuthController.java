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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // @PostMapping("/addcategories")
    // public Category addCategory(
    //         @RequestHeader("Authorization") String token,
    //         @RequestBody Category category) {
    //     return authService.addCategory(category);
    // }

    // @PostMapping("/addlocation")
    // public ResponseEntity<?> addLocation(
    //         @RequestHeader("Authorization") String token,
    //         @RequestBody Location request) {
    //     authService.addLocation(request);
    //     return ResponseEntity.ok("Location added successfully");
    // }

    // @PostMapping("/update-location-details")
    // public ResponseEntity<?> updateCategoryLocation(
    //         @RequestHeader("Authorization") String token,
    //         @RequestBody LocationUpdateRequest request) {
        
    //     return ResponseEntity.ok(
    //             authService.updateCategoryLocation(request));
    // }

    // @PutMapping("/update-category/{id}")
    // public ResponseEntity<JsonNode> updateCategory(
    //         @RequestHeader("Authorization") String token,
    //         @PathVariable int id,
    //         @RequestBody Map<String, String> body) {

    //     JsonNode updated = authService.updateCategory(
    //             id,
    //             body.get("name"),
    //             body.get("description"));

    //     return ResponseEntity.ok(updated);
    // }

    // @PutMapping("/update-location/{id}")
    // public ResponseEntity<JsonNode> updateLocation(
    //         @RequestHeader("Authorization") String token,
    //         @PathVariable int id,
    //         @RequestBody Map<String, String> body) {

    //     JsonNode updated = authService.updateLocation(
    //             id,
    //             body.get("name"),
    //             body.get("city"));

    //     return ResponseEntity.ok(updated);
    // }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return "User registered successfully";
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return new AuthResponse(token);
    }
}
