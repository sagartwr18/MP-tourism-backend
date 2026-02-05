package com.mptourism.service;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mptourism.model.Category;
import com.mptourism.model.Location;
import com.mptourism.model.LocationUpdateRequest;
import com.mptourism.storage.CategoryFileStorage;
import com.mptourism.util.JsonUpdateUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    private final CategoryFileStorage categoryFileStorage;
    private final GitHubApiService gitHubApiService;
    private final GitService gitService;

    public AdminService(CategoryFileStorage categoryFileStorage, GitHubApiService gitHubApiService, GitService gitService) {
        this.categoryFileStorage = categoryFileStorage;
        this.gitHubApiService = gitHubApiService;
        this.gitService = gitService;
    }

    public Category addCategory(Category category) {

        List<Category> categories = categoryFileStorage.loadCategories();

        int nextId = categories.isEmpty()
                ? 1
                : categories.get(categories.size() - 1).getId() + 1;

        category.setId(nextId);
        categories.add(category);

        categoryFileStorage.saveCategories(categories);
        createCategoryLocationFile(nextId);

        // Push to GitHub
        pushToGitHub("data/categories.json", "ADD CATEGORY");

        return category;
    }

    public JsonNode updateCategory(int categoryId, String name, String description) {

        try {
            File file = new File("data/categories.json");
            ArrayNode categories = (ArrayNode) mapper.readTree(file);

            String updatedName = name;
            for (JsonNode category : categories) {

                if (!category.has("id"))
                    continue;

                if (category.get("id").asInt() == categoryId) {

                    ObjectNode node = (ObjectNode) category;

                    if (name != null && !name.isBlank()) {
                        node.put("name", name);
                        updatedName = name;
                    }
                    if (description != null && !description.isBlank()) {
                        node.put("description", description);
                    }

                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValue(file, categories);

                    // Push to GitHub
                    pushToGitHub("data/categories.json", "UPDATE CATEGORY");

                    return node;
                }
            }

            throw new RuntimeException("Category not found: " + categoryId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to update category", e);
        }
    }

    public void addLocation(Location request) {

        try {
            File file = new File("data/locations.json");
            file.getParentFile().mkdirs();

            ArrayNode locations = file.exists() && file.length() > 0
                    ? (ArrayNode) mapper.readTree(file)
                    : mapper.createArrayNode();

            int nextId = 1;
            for (JsonNode loc : locations) {
                if (loc.has("id")) {
                    nextId = Math.max(nextId, loc.get("id").asInt() + 1);
                }
            }

            ObjectNode newLocation = mapper.createObjectNode();
            newLocation.put("id", nextId);
            newLocation.put("name", request.getName());
            newLocation.put("city", request.getCity());
            newLocation.put("categoryId", request.getCategoryId());

            locations.add(newLocation);

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, locations);

            createCategoryLocationFile(request.getCategoryId());

            // Push to GitHub
            pushToGitHub("data/locations.json", "ADD LOCATION");

        } catch (Exception e) {
            throw new RuntimeException("Failed to add location", e);
        }
    }

    public JsonNode updateLocation(int locationId, String name, String city) {

        try {
            File file = new File("data/locations.json");
            ArrayNode locations = (ArrayNode) mapper.readTree(file);

            String updatedName = name;
            for (JsonNode location : locations) {

                if (!location.has("id"))
                    continue;

                if (location.get("id").asInt() == locationId) {

                    ObjectNode node = (ObjectNode) location;

                    if (name != null && !name.isBlank()) {
                        node.put("name", name);
                        updatedName = name;
                    }
                    if (city != null && !city.isBlank()) {
                        node.put("city", city);
                    }

                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValue(file, locations);

                    // Push to GitHub
                    pushToGitHub("data/locations.json", "UPDATE LOCATION");

                    return node;
                }
            }

            throw new RuntimeException("Location not found: " + locationId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to update location", e);
        }
    }


    public JsonNode updateCategoryLocation(LocationUpdateRequest request) {

        try {
            String fileName = "data/category-location-details/category-" + request.getCategoryId() + ".json";
            File file = new File(fileName);

            JsonNode root = mapper.readTree(file);
            ArrayNode locations = (ArrayNode) root.get("locations");

            String locationName = "";
            for (JsonNode location : locations) {

                if (location.get("locationId").asInt() == request.getLocationId()) {
                    locationName = location.has("name") ? location.get("name").asText() : "Unknown";

                    request.getUpdates().forEach((path, value) -> JsonUpdateUtil.updateJsonValue(
                            (ObjectNode) location,
                            path,
                            mapper.valueToTree(value)));

                    mapper.writerWithDefaultPrettyPrinter()
                            .writeValue(file, root);

                    // Push to GitHub
                    pushToGitHub(fileName, "UPDATE LOCATION DETAILS");

                    return location;
                }
            }

            throw new RuntimeException("Location not found");

        } catch (Exception e) {
            throw new RuntimeException("Failed to update location", e);
        }
    }

    public List<JsonNode> addLocationToCategory(
            int categoryId,
            List<Map<String, Object>> newLocations) {

        try {
            String fileName = "data/category-location-details/category-" + categoryId + ".json";
            File file = new File(fileName);

            JsonNode root = mapper.readTree(file);
            ArrayNode locationsNode = (ArrayNode) root.get("locations");

            int maxId = 0;
            for (JsonNode loc : locationsNode) {
                maxId = Math.max(maxId, loc.get("locationId").asInt());
            }

            List<JsonNode> added = new ArrayList<>();

            for (Map<String, Object> data : newLocations) {

                ObjectNode newLoc = mapper.createObjectNode();
                newLoc.put("locationId", ++maxId);

                ObjectNode temp = mapper.valueToTree(data);

                newLoc.set("name", temp.get("name"));
                newLoc.set("details", temp.get("details"));

                locationsNode.add(newLoc);
                added.add(newLoc);
            }

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, root);

            // Push to GitHub
            pushToGitHub(fileName, "ADD LOCATIONS TO CATEGORY");

            return added;

        } catch (Exception e) {
            throw new RuntimeException("Failed to add locations", e);
        }
    }

    /*
     * =========================
     * INTERNAL HELPERS
     * =========================
     */

    /**
     * Push file changes to GitHub using GitHub API or local git
     */
    private void pushToGitHub(String fileName, String changeType) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String commitMessage = String.format("%s: %s at %s", changeType, fileName, timestamp);

            // Try GitHub API first if configured
            if (gitHubApiService.isConfigured()) {
                File file = new File(fileName);
                String content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(file));
                gitHubApiService.updateFileAsync(fileName, content, commitMessage);
            } else {
                // Fall back to local git commands
                gitService.commitAndPushAsync(fileName, changeType);
            }
        } catch (Exception e) {
            System.err.println("Failed to push to GitHub: " + e.getMessage());
        }
    }

    private void createCategoryLocationFile(int categoryId) {

        try {
            String fileName = "data/category-location-details/category-" + categoryId + ".json";
            File file = new File(fileName);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                ObjectNode root = mapper.createObjectNode();
                root.put("categoryId", categoryId);
                root.set("locations", mapper.createArrayNode());

                mapper.writerWithDefaultPrettyPrinter()
                        .writeValue(file, root);

                // Push new file to GitHub
                pushToGitHub(fileName, "ADD CATEGORY LOCATION FILE");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to create category location file", e);
        }
    }
}
