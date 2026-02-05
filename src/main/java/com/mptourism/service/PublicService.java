package com.mptourism.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mptourism.model.Category;
import com.mptourism.model.Location;
import com.mptourism.security.JwtUtil;

@Service
public class PublicService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String LOCATIONS_URL = "https://raw.githubusercontent.com/sagartwr18/MP-tourism-backend/main/data/locations.json";
    private static final String CATEGORIES_URL = "https://raw.githubusercontent.com/sagartwr18/MP-tourism-backend/main/data/categories.json";
    private static final String BASE_URL = "https://raw.githubusercontent.com/sagartwr18/MP-tourism-backend/main/data/category-location-details/";

    public List<Category> getCategories() {
        try {
            String json = restTemplate.getForObject(CATEGORIES_URL, String.class);
            return mapper.readValue(json, new TypeReference<List<Category>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load categories", e);
        }
    }

    public List<Location> getLocations() {
        try {
            String json = restTemplate.getForObject(LOCATIONS_URL, String.class);
            return mapper.readValue(json, new TypeReference<List<Location>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load locations", e);
        }
    }

    public JsonNode getLocationDetails(int categoryId, int locationId) {

        try {
            String url = BASE_URL + "category-" + categoryId + ".json";

            String json = restTemplate.getForObject(url, String.class);

            if (json == null || json.isEmpty()) {
                throw new RuntimeException("Category file is empty or missing");
            }

            JsonNode rootNode = mapper.readTree(json);

            if (!rootNode.has("locations") || !rootNode.get("locations").isArray()) {
                throw new RuntimeException("Invalid category file structure");
            }

            ArrayNode locations = (ArrayNode) rootNode.get("locations");

            for (JsonNode location : locations) {
                if (location.has("locationId")
                        && location.get("locationId").asInt() == locationId) {
                    return location;
                }
            }

            throw new RuntimeException("Location not found");

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch location details", e);
        }
    }
}
