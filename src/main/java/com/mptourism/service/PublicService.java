package com.mptourism.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mptourism.model.Category;
import com.mptourism.model.Location;
import com.mptourism.model.LocationDetail;
import com.mptourism.repository.CategoryRepository;
import com.mptourism.repository.LocationDetailRepository;
import com.mptourism.repository.LocationRepository;

@Service
public class PublicService {

    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final LocationDetailRepository locationDetailRepository;
    private final ObjectMapper mapper;

    public PublicService(
            CategoryRepository categoryRepository,
            LocationRepository locationRepository,
            LocationDetailRepository locationDetailRepository) {
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.locationDetailRepository = locationDetailRepository;
        
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public List<Category> getCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    public List<Location> getLocations() {
        return locationRepository.findAll();
    }

    public List<Location> getLocationsByCategory(int categoryId) {
        return locationRepository.findByCategoryId(categoryId);
    }

    public JsonNode getLocationDetails(int categoryId, int locationId) {
        Optional<LocationDetail> detailOpt = locationDetailRepository.findByLocationIdAndCategoryId(locationId, categoryId);
        
        if (detailOpt.isPresent()) {
            LocationDetail detail = detailOpt.get();
            try {
                return mapper.valueToTree(detail);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert location details to JSON", e);
            }
        }
        
        throw new RuntimeException("Location not found");
    }
}
