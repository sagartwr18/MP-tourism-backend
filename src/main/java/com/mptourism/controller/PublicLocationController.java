package com.mptourism.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mptourism.service.LocationDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/categories")
public class PublicLocationController {
    private final LocationDetailsService locationDetailsService;

    public PublicLocationController(LocationDetailsService locationDetailsService) {
        this.locationDetailsService = locationDetailsService;
    }

    @GetMapping("/{categoryId}/locations/{locationId}")
    public ResponseEntity<?> getLocationDetails(
        @PathVariable int categoryId,
        @PathVariable int locationId
    ) {
        JsonNode location = locationDetailsService.getLocationDetails(categoryId, locationId);
        return ResponseEntity.ok(location);
    }
    
}
