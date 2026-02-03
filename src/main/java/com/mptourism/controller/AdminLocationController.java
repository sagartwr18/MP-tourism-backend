package com.mptourism.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mptourism.model.Location;
import com.mptourism.model.Category;
import com.mptourism.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mptourism.model.LocationUpdateRequest;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminLocationController {

    private final AdminService adminService;
    @Value("${admin.secret.key}")
    private String adminSecret;

    public AdminLocationController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/addcategories")
    public Category addCategory(
            // @RequestHeader("Authorization") String token,
            @RequestBody Category category) {
        return adminService.addCategory(category);
    }

    @PostMapping("/addlocation")
    public ResponseEntity<?> addLocation(
            // @RequestHeader("Authorization") String token,
            @RequestBody Location request) {
        adminService.addLocation(request);
        return ResponseEntity.ok("Location added successfully");
    }

    @PostMapping("/categories/{categoryId}/addlocations")
    public ResponseEntity<?> addLocationToCategory(
            // @RequestHeader("Authorization") String token,
            @RequestHeader("X-ADMIN-KEY") String secret,
            @PathVariable int categoryId,
            @RequestBody LocationUpdateRequest request) {
        List<JsonNode> addedLocations = adminService.addLocationToCategory(categoryId, request.getLocations());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Locations added Successfully",
                        "addedCount", addedLocations.size(),
                        "locations", addedLocations));
    }

    @PostMapping("/update-location-details")
    public ResponseEntity<?> updateCategoryLocation(
            @RequestHeader("X-ADMIN-KEY") String secret,
            @RequestBody LocationUpdateRequest request) {
        if (!adminSecret.equals(secret)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        return ResponseEntity.ok(
                adminService.updateCategoryLocation(request));
    }

    @PutMapping("/update-category/{id}")
    public ResponseEntity<JsonNode> updateCategory(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {

        JsonNode updated = adminService.updateCategory(
                id,
                body.get("name"),
                body.get("description"));

        return ResponseEntity.ok(updated);
    }

    @PutMapping("/update-location/{id}")
    public ResponseEntity<JsonNode> updateLocation(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {

        JsonNode updated = adminService.updateLocation(
                id,
                body.get("name"),
                body.get("city"));

        return ResponseEntity.ok(updated);
    }
}
