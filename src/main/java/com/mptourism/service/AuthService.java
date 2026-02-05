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
    private static final String USERS_FILE = "https://raw.githubusercontent.com/sagartwr18/MP-tourism-backend/main/data/locations.json";

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
}