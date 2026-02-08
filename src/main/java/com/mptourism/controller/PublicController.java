package com.mptourism.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.mptourism.model.Category;
import com.mptourism.model.Location;
import com.mptourism.service.PublicService;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicService publicService;

    public PublicController(PublicService publicService) {
        this.publicService = publicService;
    }

    // ✅ GET ALL CATEGORIES
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(publicService.getCategories());
    }

    // ✅ GET LOCATIONS BY CATEGORY
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Location>> getByCategory(@PathVariable int categoryId) {
        return ResponseEntity.ok(publicService.getLocationsByCategory(categoryId));
    }

    @GetMapping("/{categoryId}/location/{locationId}")
    public ResponseEntity<?> getLocationDetails(
        @PathVariable int categoryId,
        @PathVariable int locationId
    ) {
        return ResponseEntity.ok(
            publicService.getLocationDetails(categoryId, locationId)
        );
    }
}
