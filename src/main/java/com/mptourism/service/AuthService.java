package com.mptourism.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mptourism.model.Category;
import com.mptourism.model.Location;
import com.mptourism.model.LocationUpdateRequest;
import com.mptourism.model.LoginRequest;
import com.mptourism.model.RegisterRequest;
import com.mptourism.model.User;
import com.mptourism.security.JwtUtil;
import com.mptourism.storage.CategoryFileStorage;
import com.mptourism.util.JsonUpdateUtil;

// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

@Service
public class AuthService {

    // private final PasswordEncoder passwordEncoder;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CategoryFileStorage categoryStorage;
    private static final String USERS_FILE = "data/users.json";

    public AuthService(CategoryFileStorage categoryStorage) {
        this.categoryStorage = categoryStorage;
    }

    public void register(RegisterRequest request) {

        try {
            File file = new File(USERS_FILE);
            file.getParentFile().mkdirs();

            List<User> users;

            if (!file.exists() || file.length() == 0) {
                users = new ArrayList<>();
            } else {
                users = mapper.readValue(file, new TypeReference<>() {
                });
            }

            for (User u : users) {
                if (u.getUsername().equalsIgnoreCase(request.getUsername())) {
                    throw new RuntimeException("Username already exists");
                }
            }

            User user = new User();
            user.setId(users.size() + 1);
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword()); // plain for now

            users.add(user);

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);

        } catch (Exception e) {
            throw new RuntimeException("Registration failed", e);
        }
    }

    public String login(LoginRequest request) {

        try {
            File file = new File(USERS_FILE);

            if (!file.exists() || file.length() == 0) {
                throw new RuntimeException("No users registered");
            }

            List<User> users = mapper.readValue(file, new TypeReference<>() {
            });

            for (User u : users) {
                if (u.getUsername().equalsIgnoreCase(request.getUsername())
                        && u.getPassword().equals(request.getPassword())) {

                    return JwtUtil.generateToken(u.getUsername());
                }
            }

            throw new RuntimeException("Invalid username or password");

        } catch (Exception e) {
            throw new RuntimeException("Login failed", e);
        }
    }

    // public Category addCategory(Category category) {

    // List<Category> categories = categoryStorage.loadCategories();

    // // Auto-generate ID
    // int nextId = categories.isEmpty()
    // ? 1
    // : categories.get(categories.size() - 1).getId() + 1;

    // category.setId(nextId);
    // categories.add(category);

    // // Save categories.json
    // categoryStorage.saveCategories(categories);

    // // Create empty location file for this category
    // createCategoryLocationFile(nextId);

    // return category;
    // }

    // public JsonNode updateCategory(int categoryId, String name, String
    // description) {

    // try {
    // File file = new File("data/categories.json");

    // ArrayNode categories = (ArrayNode) mapper.readTree(file);

    // for (JsonNode category : categories) {

    // // ✅ Correct key: "id"
    // JsonNode idNode = category.get("id");

    // if (idNode == null || !idNode.isInt()) {
    // continue; // safety: skip malformed entries
    // }

    // if (idNode.asInt() == categoryId) {

    // ObjectNode categoryNode = (ObjectNode) category;

    // if (name != null && !name.isBlank()) {
    // categoryNode.put("name", name);
    // }

    // if (description != null && !description.isBlank()) {
    // categoryNode.put("description", description);
    // }

    // mapper.writerWithDefaultPrettyPrinter()
    // .writeValue(file, categories);

    // return categoryNode;
    // }
    // }

    // throw new RuntimeException("Category not found with id: " + categoryId);

    // } catch (Exception e) {
    // throw new RuntimeException("Failed to update category", e);
    // }
    // }

    // public void addLocation(Location request) {

    // try {
    // File file = new File("data/locations.json");
    // file.getParentFile().mkdirs();

    // ArrayNode locationsArray;

    // // 1️⃣ If file does not exist or is empty → create new array
    // if (!file.exists() || file.length() == 0) {
    // locationsArray = mapper.createArrayNode();
    // } else {
    // JsonNode json = mapper.readTree(file);

    // if (!json.isArray()) {
    // throw new RuntimeException("Invalid locations.json structure. Expected JSON
    // Array.");
    // }

    // locationsArray = (ArrayNode) json;
    // }

    // // 2️⃣ Generate next ID
    // int nextId = 1;
    // for (JsonNode loc : locationsArray) {
    // if (loc.has("id")) {
    // nextId = Math.max(nextId, loc.get("id").asInt() + 1);
    // }
    // }

    // // 3️⃣ Create new location node
    // ObjectNode newLocation = mapper.createObjectNode();
    // newLocation.put("id", nextId);
    // newLocation.put("name", request.getName());
    // newLocation.put("city", request.getCity());
    // newLocation.put("categoryId", request.getCategoryId());

    // // 4️⃣ Add to array
    // locationsArray.add(newLocation);

    // // 5️⃣ Write back to file
    // mapper
    // .writerWithDefaultPrettyPrinter()
    // .writeValue(file, locationsArray);

    // // 6️⃣ Ensure category file exists (if needed)
    // createCategoryLocationFile(request.getCategoryId());

    // } catch (Exception e) {
    // throw new RuntimeException("Failed to add location", e);
    // }
    // }

    // public JsonNode updateLocation(int locationId, String name, String city) {

    // try {
    // File file = new File("data/locations.json");

    // ArrayNode locations = (ArrayNode) mapper.readTree(file);

    // for (JsonNode location : locations) {

    // // ✅ Correct key: "id"
    // JsonNode idNode = location.get("id");

    // if (idNode == null || !idNode.isInt()) {
    // continue; // safety: skip malformed entries
    // }

    // if (idNode.asInt() == locationId) {

    // ObjectNode locationNode = (ObjectNode) location;

    // if (name != null && !name.isBlank()) {
    // locationNode.put("name", name);
    // }

    // if (city != null && !city.isBlank()) {
    // locationNode.put("city", city);
    // }

    // mapper.writerWithDefaultPrettyPrinter()
    // .writeValue(file, locations);

    // return locationNode;
    // }
    // }

    // throw new RuntimeException("Category not found with id: " + locationId);

    // } catch (Exception e) {
    // throw new RuntimeException("Failed to update category", e);
    // }
    // }

    // public JsonNode updateCategoryLocation(LocationUpdateRequest request) {

    // try {
    // File file = new File(
    // "data/category-location-details/category-" + request.getCategoryId() +
    // ".json");

    // if (!file.exists()) {
    // throw new RuntimeException("Category file not found");
    // }

    // JsonNode root = mapper.readTree(file);
    // ArrayNode locations = (ArrayNode) root.get("locations");

    // if (locations == null) {
    // throw new RuntimeException("Locations array missing");
    // }

    // for (JsonNode location : locations) {
    // if (location.get("locationId").asInt() == request.getLocationId()) {

    // request.getUpdates().forEach((path, value) -> {
    // JsonUpdateUtil.updateJsonValue(
    // (ObjectNode) location,
    // path,
    // mapper.valueToTree(value));
    // });

    // mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);

    // return location;
    // }
    // }

    // throw new RuntimeException("Location Not Found");

    // } catch (Exception e) {
    // throw new RuntimeException("Failed to Update Location", e);
    // }
    // }

    // private void createCategoryLocationFile(int categoryId) {
    // try {
    // File file = new File(
    // "data/category-location-details/category-" + categoryId + ".json");

    // if (!file.getParentFile().exists()) {
    // file.getParentFile().mkdirs();
    // }

    // if (!file.exists()) {

    // String initialJson = """
    // {
    // "categoryId" : %d,
    // "locations" : []
    // }
    // """.formatted(categoryId);

    // mapper
    // .writerWithDefaultPrettyPrinter()
    // .writeValue(file, mapper.readTree(initialJson));
    // }
    // } catch (Exception e) {
    // throw new RuntimeException("Failed to create category location file", e);
    // }
    // }

}
