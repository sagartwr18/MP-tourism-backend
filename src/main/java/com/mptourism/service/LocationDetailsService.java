package com.mptourism.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class LocationDetailsService {
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    public JsonNode getLocationDetails(int categoryId, int locationId) {

        try {
            File file = new File(
                "data/category-location-details/category-" + categoryId + ".json"
            );

            if(!file.exists()) {
                throw new RuntimeException("File not Found");
            }

            JsonNode rootNode = mapper.readTree(file);
            
            if(!rootNode.has("locations") || !rootNode.get("locations").isArray()) {
                throw new RuntimeException("Invalid Category File Structure");
            }

            ArrayNode locations = (ArrayNode) rootNode.get("locations");

            for(JsonNode location : locations) {
                if(location.has("locationId") && location.get("locationId").asInt() == locationId) {
                    return location;
                }
            }

            throw new RuntimeException("Location Not Found");
        } catch (Exception e) {
            throw new RuntimeException("Failed to Fetch Location Details", e);
        }
    }
}
