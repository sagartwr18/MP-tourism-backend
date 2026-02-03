package com.mptourism.service;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mptourism.model.Category;
import com.mptourism.model.Location;
import com.mptourism.model.LocationUpdateRequest;
import com.mptourism.util.JsonUpdateUtil;
import com.mptourism.storage.CategoryFileStorage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Service
public class AdminService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final CategoryFileStorage categoryStorage;

    public AdminService(CategoryFileStorage categoryStorage) {
        this.categoryStorage = categoryStorage;
    }

    public Category addCategory(Category category) {

        List<Category> categories = categoryStorage.loadCategories();

        // Auto-generate ID
        int nextId = categories.isEmpty()
                ? 1
                : categories.get(categories.size() - 1).getId() + 1;

        category.setId(nextId);
        categories.add(category);

        // Save categories.json
        categoryStorage.saveCategories(categories);

        // Create empty location file for this category
        createCategoryLocationFile(nextId);

        return category;
    }

    public JsonNode updateCategory(int categoryId, String name, String description) {

        try {
            File file = new File("data/categories.json");

            ArrayNode categories = (ArrayNode) mapper.readTree(file);

            for (JsonNode category : categories) {

                // ✅ Correct key: "id"
                JsonNode idNode = category.get("id");

                if (idNode == null || !idNode.isInt()) {
                    continue; // safety: skip malformed entries
                }

                if (idNode.asInt() == categoryId) {

                    ObjectNode categoryNode = (ObjectNode) category;

                    if (name != null && !name.isBlank()) {
                        categoryNode.put("name", name);
                    }

                    if (description != null && !description.isBlank()) {
                        categoryNode.put("description", description);
                    }

                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValue(file, categories);

                    return categoryNode;
                }
            }

            throw new RuntimeException("Category not found with id: " + categoryId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to update category", e);
        }
    }

    public void addLocation(Location request) {

        try {
            File file = new File("data/locations.json");
            file.getParentFile().mkdirs();

            ArrayNode locationsArray;

            // 1️⃣ If file does not exist or is empty → create new array
            if (!file.exists() || file.length() == 0) {
                locationsArray = mapper.createArrayNode();
            } else {
                JsonNode json = mapper.readTree(file);

                if (!json.isArray()) {
                    throw new RuntimeException("Invalid locations.json structure. Expected JSON Array.");
                }

                locationsArray = (ArrayNode) json;
            }

            // 2️⃣ Generate next ID
            int nextId = 1;
            for (JsonNode loc : locationsArray) {
                if (loc.has("id")) {
                    nextId = Math.max(nextId, loc.get("id").asInt() + 1);
                }
            }

            // 3️⃣ Create new location node
            ObjectNode newLocation = mapper.createObjectNode();
            newLocation.put("id", nextId);
            newLocation.put("name", request.getName());
            newLocation.put("city", request.getCity());
            newLocation.put("categoryId", request.getCategoryId());

            // 4️⃣ Add to array
            locationsArray.add(newLocation);

            // 5️⃣ Write back to file
            mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(file, locationsArray);

            // 6️⃣ Ensure category file exists (if needed)
            createCategoryLocationFile(request.getCategoryId());

        } catch (Exception e) {
            throw new RuntimeException("Failed to add location", e);
        }
    }

    public JsonNode updateLocation(int locationId, String name, String city) {

        try {
            File file = new File("data/locations.json");

            ArrayNode locations = (ArrayNode) mapper.readTree(file);

            for (JsonNode location : locations) {

                // ✅ Correct key: "id"
                JsonNode idNode = location.get("id");

                if (idNode == null || !idNode.isInt()) {
                    continue; // safety: skip malformed entries
                }

                if (idNode.asInt() == locationId) {

                    ObjectNode locationNode = (ObjectNode) location;

                    if (name != null && !name.isBlank()) {
                        locationNode.put("name", name);
                    }

                    if (city != null && !city.isBlank()) {
                        locationNode.put("city", city);
                    }

                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValue(file, locations);

                    return locationNode;
                }
            }

            throw new RuntimeException("Category not found with id: " + locationId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to update category", e);
        }
    }

    public JsonNode updateCategoryLocation(LocationUpdateRequest request) {

        try {
            File file = new File(
                    "data/category-location-details/category-" + request.getCategoryId() + ".json");

            if (!file.exists()) {
                throw new RuntimeException("Category file not found");
            }

            JsonNode root = mapper.readTree(file);
            ArrayNode locations = (ArrayNode) root.get("locations");

            if (locations == null) {
                throw new RuntimeException("Locations array missing");
            }

            for (JsonNode location : locations) {
                if (location.get("locationId").asInt() == request.getLocationId()) {

                    request.getUpdates().forEach((path, value) -> {
                        JsonUpdateUtil.updateJsonValue(
                                (ObjectNode) location,
                                path,
                                mapper.valueToTree(value));
                    });

                    mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);

                    return location;
                }
            }

            throw new RuntimeException("Location Not Found");

        } catch (Exception e) {
            throw new RuntimeException("Failed to Update Location", e);
        }
    }

    public List<JsonNode> addLocationToCategory(
            int categoryId,
            List<Map<String, Object>> newLocations) {

        if (newLocations == null || newLocations.isEmpty()) {
            throw new IllegalArgumentException("Locations list cannot be null or empty");
        }

        try {
            File file = new File("data/category-location-details/category-" + categoryId + ".json");

            if (!file.exists()) {
                throw new RuntimeException("Category file not found");
            }

            JsonNode root = mapper.readTree(file);
            ArrayNode locationsNode = (ArrayNode) root.get("locations");

            if (locationsNode == null) {
                throw new RuntimeException("Locations array missing in category file");
            }

            int maxId = 0;
            for (JsonNode loc : locationsNode) {
                if (loc.has("locationId")) {
                    maxId = Math.max(maxId, loc.get("locationId").asInt());
                }
            }

            List<JsonNode> added = new ArrayList<>();

            for (Map<String, Object> locData : newLocations) {

                ObjectNode temp = mapper.valueToTree(locData);

                temp.remove("locationId");

                maxId++;

                ObjectNode newLoc = mapper.createObjectNode();

                newLoc.put("locationId", maxId);

                if (temp.has("name")) {
                    newLoc.set("name", temp.get("name"));
                } else {
                    throw new RuntimeException("Location name is required");
                }

                if (temp.has("details")) {
                    newLoc.set("details", temp.get("details"));
                } else {
                    throw new RuntimeException("Location details are required");
                }

                locationsNode.add(newLoc);
                added.add(newLoc);
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);

            return added;

        } catch (Exception e) {
            throw new RuntimeException("Failed to add locations", e);
        }
    }

    private void createCategoryLocationFile(int categoryId) {
        try {
            File file = new File(
                    "data/category-location-details/category-" + categoryId + ".json");

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {

                String initialJson = """
                        {
                            "categoryId" : %d,
                            "locations" : []
                        }
                        """.formatted(categoryId);

                mapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValue(file, mapper.readTree(initialJson));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create category location file", e);
        }
    }
}
