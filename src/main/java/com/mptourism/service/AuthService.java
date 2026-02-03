package com.mptourism.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mptourism.model.User;
import com.mptourism.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String USERS_FILE = "data/users.json";

    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User request) {

        try {
            File file = new File(USERS_FILE);
            file.getParentFile().mkdirs();

            List<User> users;

            if (!file.exists() || file.length() == 0) {
                users = new ArrayList<>();
            } else {
                users = mapper.readValue(file, new TypeReference<List<User>>() {
                });
            }

            // Check duplicate username
            for (User u : users) {
                if (u.getUsername().equalsIgnoreCase(request.getUsername())) {
                    throw new RuntimeException("Username already exists");
                }
            }

            // Generate ID
            int nextId = users.isEmpty()
                    ? 1
                    : users.get(users.size() - 1).getId() + 1;

            request.setId(nextId);
            request.setPassword(request.getPassword());

            users.add(request);

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);

        } catch (Exception e) {
            throw new RuntimeException("Failed to register user", e);
        }
    }

    public String loginUser(String username, String password) {

        try {
            File file = new File(USERS_FILE);

            if (!file.exists() || file.length() == 0) {
                throw new RuntimeException("No users registered");
            }

            List<User> users = mapper.readValue(file, new TypeReference<List<User>>() {
            });

            for (User u : users) {
                if (u.getUsername().equalsIgnoreCase(username)
                        && u.getPassword().equals(password)) {

                    return JwtUtil.generateToken(u.getUsername());
                }
            }

            throw new RuntimeException("Invalid username or password");

        } catch (Exception e) {
            throw new RuntimeException("Login failed", e);
        }
    }

}
