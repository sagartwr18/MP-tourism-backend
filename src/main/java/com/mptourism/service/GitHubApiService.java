package com.mptourism.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Service
public class GitHubApiService {

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
        try {
            // Get current file SHA
            String sha = getFileSha(filePath);

            String url = "https://api.github.com/repos/" + repo + "/contents/" + filePath;

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

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return "Successfully updated " + filePath + " on GitHub";
            } else {
                return "GitHub API Error: " + response.body();
            }

        } catch (Exception e) {
            return "Error updating GitHub: " + e.getMessage();
        }
    }

    /**
     * Get the SHA of a file from GitHub (needed for updates)
     */
    private String getFileSha(String filePath) {
        try {
            String url = "https://api.github.com/repos/" + repo + "/contents/" + filePath + "?ref=" + branch;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("Authorization", "Bearer " + githubToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = mapper.readTree(response.body());
                return json.has("sha") ? json.get("sha").asText() : null;
            }
        } catch (Exception e) {
            // File might not exist yet
        }
        return null;
    }

    /**
     * Check if GitHub API is configured
     */
    public boolean isConfigured() {
        return githubToken != null && !githubToken.isEmpty();
    }
}
