package com.mptourism.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mptourism.model.LoginRequest;
import com.mptourism.model.RegisterRequest;
import com.mptourism.model.User;
import com.mptourism.security.JwtUtil;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final GitHubApiService gitHubApiService;
    
    private static final String USERS_URL = "https://raw.githubusercontent.com/sagartwr18/MP-tourism-backend/main/data/users.json";
    private static final String USERS_FILE = "data/users.json";

    public AuthService(GitHubApiService gitHubApiService) {
        this.gitHubApiService = gitHubApiService;
    }

    public void register(RegisterRequest request) {

        try {
            // Read current users from GitHub
            List<User> users = getUsersFromGitHub();

            for (User u : users) {
                if (u.getUsername().equalsIgnoreCase(request.getUsername())) {
                    throw new RuntimeException("Username already exists");
                }
            }

            User user = new User();
            user.setId(users.size() + 1);
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());

            users.add(user);

            // Save locally first
            File file = new File(USERS_FILE);
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);

            // Push to GitHub
            String content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                mapper.readTree(mapper.writeValueAsString(users))
            );
            String commitMessage = String.format("ADD USER: %s at %s", 
                request.getUsername(), 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
            
            gitHubApiService.updateFileAsync(USERS_FILE, content, commitMessage);

        } catch (Exception e) {
            throw new RuntimeException("Registration failed", e);
        }
    }

    public String login(LoginRequest request) {

        try {
            // Read users from GitHub
            List<User> users = getUsersFromGitHub();

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

    private List<User> getUsersFromGitHub() {
        try {
            String json = restTemplate.getForObject(USERS_URL, String.class);
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            
            ArrayNode arrayNode = (ArrayNode) mapper.readTree(json);
            List<User> users = new ArrayList<>();
            for (var node : arrayNode) {
                User user = mapper.treeToValue(node, User.class);
                users.add(user);
            }
            return users;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
