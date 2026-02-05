package com.mptourism.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class GitService {

    private static final String PROJECT_ROOT = System.getProperty("user.dir");

    /**
     * Asynchronously stages, commits, and pushes changes to git
     * 
     * @param fileName The file that was modified
     * @param changeType Type of change: "ADD", "UPDATE", or "DELETE"
     */
    @Async
    public CompletableFuture<String> commitAndPushAsync(String fileName, String changeType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return commitAndPush(fileName, changeType);
            } catch (Exception e) {
                return "Failed to commit and push: " + e.getMessage();
            }
        });
    }

    /**
     * Stages, commits, and pushes changes to git synchronously
     */
    public String commitAndPush(String fileName, String changeType) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String commitMessage = String.format("%s: %s at %s", changeType, fileName, timestamp);

            // Stage the file
            runGitCommand("git", "add", fileName);

            // Commit the changes
            runGitCommand("git", "commit", "-m", commitMessage);

            // Push to remote
            runGitCommand("git", "push");

            return String.format("Successfully committed and pushed %s: %s", fileName, commitMessage);

        } catch (Exception e) {
            return "Error during git operation: " + e.getMessage();
        }
    }

    /**
     * Stages all changes and commits with an auto-generated message
     */
    @Async
    public CompletableFuture<String> autoCommitAllAsync(String description) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String commitMessage = String.format("Auto-update: %s at %s", description, timestamp);

                runGitCommand("git", "add", ".");
                runGitCommand("git", "commit", "-m", commitMessage);
                runGitCommand("git", "push");

                return "Successfully auto-committed and pushed: " + commitMessage;

            } catch (Exception e) {
                return "Error during auto-commit: " + e.getMessage();
            }
        });
    }

    private void runGitCommand(String... command) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new File(PROJECT_ROOT));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Read output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Git] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Git command failed with exit code: " + exitCode);
        }
    }

    /**
     * Check if git is available and repository is properly configured
     */
    public boolean isGitConfigured() {
        try {
            runGitCommand("git", "status");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
