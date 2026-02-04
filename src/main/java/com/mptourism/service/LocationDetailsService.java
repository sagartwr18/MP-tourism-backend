package com.mptourism.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LocationDetailsService {

    private static final String BASE_URL =
        "https://raw.githubusercontent.com/sagartwr18/MP-tourism-backend/main/data/category-location-details/";

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

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
