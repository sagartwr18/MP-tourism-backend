package com.mptourism.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mptourism.model.User;
import com.mptourism.model.Category;
import com.mptourism.model.Location;
import com.mptourism.security.JwtUtil;

@Service
public class PublicService {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final String CATEGORIES_FILE = "https://github.com/sagartwr18/MP-tourism-backend/blob/main/data/categories.json";
    private static final String LOCATIONS_FILE = "https://github.com/sagartwr18/MP-tourism-backend/blob/main/data/locations.json";

    public List<Category> getCategories() {
        try {
            File file = new File(CATEGORIES_FILE);
            if (!file.exists())
                return List.of();
            return mapper.readValue(file, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load categories", e);
        }
    }

    public List<Location> getLocations() {
        try {
            File file = new File(LOCATIONS_FILE);
            if (!file.exists())
                return List.of();
            return mapper.readValue(file, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load locations", e);
        }
    }
}
