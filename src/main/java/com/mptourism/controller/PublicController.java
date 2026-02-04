package com.mptourism.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.mptourism.data.CategoryData;
import com.mptourism.data.LocationData;
import com.mptourism.model.Category;
import com.mptourism.model.Location;
import com.mptourism.service.LocationDetailsService;
import com.mptourism.service.PublicService;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicService publicService;
    private final LocationDetailsService locationDetailsService;

    public PublicController(PublicService publicService, LocationDetailsService locationDetailsService) {
        this.publicService = publicService;
        this.locationDetailsService = locationDetailsService;
    }

    // GET all categories
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return publicService.getCategories();
    }

    @GetMapping("/category/{categoryId}")
    public List<Location> getByCategory(@PathVariable int categoryId) {
        return publicService.getLocations()
                .stream()
                .filter(l -> l.getCategoryId() == categoryId)
                .toList();
    }

    @GetMapping("/{categoryId}/locations/{locationId}")
    public ResponseEntity<?> getLocationDetails(
        @PathVariable int categoryId,
        @PathVariable int locationId
    ) {
        return ResponseEntity.ok(
            locationDetailsService.getLocationDetails(categoryId, locationId)
        );
    }
}
