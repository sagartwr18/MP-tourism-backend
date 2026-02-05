package com.mptourism.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Service
public class GitHubApiService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubApiService.class);

    @Value("${github.token:}")
    private String githubToken;

    @Value("${github.repo:sagartwr18/MP-tourism-backend}")
    private String repo;

    @Value("${github.branch:main}")
    private String branch;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Asynchronously update a file in GitHub using GitHub API
     */
    @Async
    public CompletableFuture<String> updateFileAsync(String filePath, String content, String commitMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return updateFile(filePath, content, commitMessage);
            } catch (Exception e) {
                return "Failed to update GitHub: " + e.getMessage();
            }
        });
    }

    /**
     * Update a file in GitHub using GitHub API
     */
    public String updateFile(String filePath, String content, String commitMessage) {
        logger.info("GitHubApiService.updateFile called for: {}", filePath);
        logger.info("GitHub token configured: {}", !githubToken.isEmpty());
        
        if (githubToken == null || githubToken.isEmpty()) {
            logger.error("GitHub token is not configured!");
            return "GitHub token not configured";
        }

        try {
            // Get current file SHA
            String sha = getFileSha(filePath);
            logger.info("File SHA: {}", sha);

            String url = "https://api.github.com/repos/" + repo + "/contents/" + filePath;
            logger.info("GitHub API URL: {}", url);

            String requestBody;
            if (sha != null) {
                requestBody = String.format(
                    "{\"message\":\"%s\",\"content\":\"%s\",\"branch\":\"%s\",\"sha\":\"%s\"}",
                    commitMessage,
                    Base64.getEncoder().encodeToString(content.getBytes()),
                    branch,
                    sha
                );
            } else {
                requestBody = String.format(
                    "{\"message\":\"%s\",\"content\":\"%s\",\"branch\":\"%s\"}",
                    commitMessage,
                    Base64.getEncoder().encodeToString(content.getBytes()),
                    branch
                );
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("Authorization", "Bearer " + githubToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            logger.info("Sending request to GitHub API...");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info("GitHub API response status: {}", response.statusCode());
            logger.info("GitHub API response body: {}", response.body());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return "Successfully updated " + filePath + " on GitHub";
            } else {
                return "GitHub API Error: " + response.body();
            }

        } catch (Exception e) {
            logger.error("Error updating GitHub: {}", e.getMessage(), e);
            return "Error updating GitHub: " + e.getMessage();
        }
    }

    /**
     * Get the SHA of a file from GitHub (needed for updates)
     */
    private String getFileSha(String filePath) {
        try {
            String url = "https://api.github.com/repos/" + repo + "/contents/" + filePath + "?ref=" + branch;
            logger.info("Getting file SHA from: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("Authorization", "Bearer " + githubToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info("getFileSha response status: {}", response.statusCode());
            
            if (response.statusCode() == 200) {
                JsonNode json = mapper.readTree(response.body());
                String sha = json.has("sha") ? json.get("sha").asText() : null;
                logger.info("File SHA retrieved: {}", sha);
                return sha;
            }
        } catch (Exception e) {
            logger.warn("Could not get file SHA (file might not exist): {}", e.getMessage());
        }
        return null;
    }

    /**
     * Check if GitHub API is configured
     */
    public boolean isConfigured() {
        boolean configured = githubToken != null && !githubToken.isEmpty();
        logger.info("GitHubApiService.isConfigured(): {}", configured);
        return configured;
    }
}
