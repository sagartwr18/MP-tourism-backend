package com.mptourism.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mptourism.model.Category;
import com.mptourism.model.Location;
import com.mptourism.model.LocationDetail;
import com.mptourism.model.LocationUpdateRequest;
import com.mptourism.repository.CategoryRepository;
import com.mptourism.repository.LocationDetailRepository;
import com.mptourism.repository.LocationRepository;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {

    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final LocationDetailRepository locationDetailRepository;
    private final ObjectMapper mapper;

    public AdminService(
            CategoryRepository categoryRepository,
            LocationRepository locationRepository,
            LocationDetailRepository locationDetailRepository
        ) {
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.locationDetailRepository = locationDetailRepository;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Category addCategory(Category category) {
        // Don't set ID manually - let JPA auto-generate it
        Category saved = categoryRepository.save(category);
        return saved;
    }

    public JsonNode updateCategory(int categoryId, String name, String description) {
        Optional<Category> catOpt = categoryRepository.findById(categoryId);

        if (catOpt.isEmpty()) {
            throw new RuntimeException("Category not found: " + categoryId);
        }

        Category category = catOpt.get();
        if (name != null && !name.isBlank()) {
            category.setName(name);
        }
        if (description != null && !description.isBlank()) {
            category.setDescription(description);
        }

        Category updated = categoryRepository.save(category);

        try {
            return mapper.valueToTree(updated);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    public JsonNode updateCategoryLocation(LocationUpdateRequest request) {
        // Find and update in locations table
        Optional<Location> locOpt = locationRepository.findById(request.getLocationId());
        if (locOpt.isEmpty()) {
            throw new RuntimeException("Location not found: " + request.getLocationId());
        }
        
        Location location = locOpt.get();
        
        // Find and update in location_info table
        Optional<LocationDetail> detailOpt = locationDetailRepository.findByLocationIdAndCategoryId(
                request.getLocationId(), request.getCategoryId());
        if (detailOpt.isEmpty()) {
            throw new RuntimeException("Location detail not found for location_id: " + request.getLocationId());
        }
        
        LocationDetail detail = detailOpt.get();
        
        // Get the updates map
        Map<String, Object> updates = request.getUpdates();
        
        if (updates != null) {
            // Update name in both tables
            if (updates.containsKey("name")) {
                Object nameValue = updates.get("name");
                if (nameValue instanceof String) {
                    String name = (String) nameValue;
                    location.setName(name);
                    detail.setName(name);
                }
            }
            
            // Update district in locations table
            if (updates.containsKey("district")) {
                Object districtValue = updates.get("district");
                if (districtValue instanceof String) {
                    String district = (String) districtValue;
                    location.setCity(district);
                }
            }
            
            // Update details (JSONB) in location_info table
            if (updates.containsKey("details")) {
                Object detailsObj = updates.get("details");
                if (detailsObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> newDetails = (Map<String, Object>) detailsObj;
                    Map<String, Object> currentDetails = detail.getDetails();
                    if (currentDetails == null) {
                        currentDetails = new HashMap<>();
                    }
                    currentDetails.putAll(newDetails);
                    detail.setDetails(currentDetails);
                }
            }
        }
        
        // Save both entities
        locationRepository.save(location);
        LocationDetail updatedDetail = locationDetailRepository.save(detail);
        
        try {
            return mapper.valueToTree(updatedDetail);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }

    public List<JsonNode> addLocationToCategory(int categoryId, List<Map<String, Object>> newLocations) {
        List<JsonNode> added = new java.util.ArrayList<>();

        for (Map<String, Object> data : newLocations) {
            // Extract location data
            String name = (String) data.get("name");
            String district = (String) data.get("district");
            
            // Create location in locations table
            Location location = new Location();
            location.setName(name);
            location.setCity(district);
            location.setCategoryId(categoryId);
            Location savedLocation = locationRepository.save(location);
            
            // Create location detail in location_info table
            LocationDetail detail = new LocationDetail();
            detail.setLocationId(savedLocation.getId());
            detail.setName(name);
            detail.setCategoryId(categoryId);
            
            // Extract and set the details (JSONB data)
            Object detailsObj = data.get("details");
            if (detailsObj instanceof Map) {
                detail.setDetails((Map<String, Object>) detailsObj);
            } else {
                detail.setDetails(new HashMap<>());
            }
            
            LocationDetail savedDetail = locationDetailRepository.save(detail);
            added.add(mapper.valueToTree(savedDetail));
        }

        return added;
    }

    private int getNextCategoryId() {
        List<Category> categories = categoryRepository.findAll();
        return categories.isEmpty() ? 1
                : categories.stream()
                        .mapToInt(Category::getId)
                        .max()
                        .getAsInt() + 1;
    }

    private int getNextLocationId() {
        List<Location> locations = locationRepository.findAll();
        return locations.isEmpty() ? 1
                : locations.stream()
                        .mapToInt(Location::getId)
                        .max()
                        .getAsInt() + 1;
    }

    private int getNextLocationDetailId() {
        List<LocationDetail> details = locationDetailRepository.findAll();
        return details.isEmpty() ? 1
                : details.stream()
                        .mapToInt(LocationDetail::getId)
                        .max()
                        .getAsInt() + 1;
    }
}
