package com.mptourism.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mptourism.model.Location;
import com.mptourism.model.Category;
import com.mptourism.model.LocationUpdateRequest;
import com.mptourism.service.AdminService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ✅ ADD CATEGORY
    @PostMapping("/addcategory")
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        return ResponseEntity.ok(adminService.addCategory(category));
    }

    // ✅ ADD LOCATION
    @PostMapping("/addlocation")
    public ResponseEntity<?> addLocation(@RequestBody Location request) {
        adminService.addLocation(request);
        return ResponseEntity.ok("Location added successfully");
    }

    // ✅ ADD MULTIPLE LOCATIONS TO CATEGORY
    @PostMapping("/category/{categoryId}/locations")
    public ResponseEntity<?> addLocationToCategory(
            @PathVariable int categoryId,
            @RequestBody LocationUpdateRequest request) {

        List<JsonNode> addedLocations =
                adminService.addLocationToCategory(categoryId, request.getLocations());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Locations added successfully",
                        "addedCount", addedLocations.size(),
                        "locations", addedLocations
                )
        );
    }

    // ✅ UPDATE CATEGORY DETAILS
    @PutMapping("/editcategory/{id}")
    public ResponseEntity<JsonNode> updateCategory(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {

        JsonNode updated = adminService.updateCategory(
                id,
                body.get("name"),
                body.get("description")
        );

        return ResponseEntity.ok(updated);
    }

    // ✅ UPDATE LOCATION DETAILS
    @PutMapping("/editlocation/{id}")
    public ResponseEntity<JsonNode> updateLocation(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {

        JsonNode updated = adminService.updateLocation(
                id,
                body.get("name"),
                body.get("city")
        );

        return ResponseEntity.ok(updated);
    }

    // ✅ UPDATE CATEGORY LOCATION DETAILS (BULK)
    @PutMapping("/editlocation/update-details")
    public ResponseEntity<?> updateCategoryLocation(
            @RequestBody LocationUpdateRequest request) {

        return ResponseEntity.ok(
                adminService.updateCategoryLocation(request)
        );
    }
}
