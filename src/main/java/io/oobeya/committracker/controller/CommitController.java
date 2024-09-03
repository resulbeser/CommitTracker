package io.oobeya.committracker.controller;

import io.oobeya.committracker.dto.CommitsRequest;
import io.oobeya.committracker.service.GitHubService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class CommitController {
    private final GitHubService gitHubService;

    public CommitController() {
        this.gitHubService = new GitHubService();
    }

    public void displayCommits() {
        CommitsRequest request = new CommitsRequest("your-username", "your-repo-name");
        List<JsonNode> commits = gitHubService.getCommits(request);

        for (JsonNode commit : commits) {
            String sha = commit.get("sha").asText();
            String message = commit.get("commit").get("message").asText();
            String author = commit.get("commit").get("author").get("name").asText();
            System.out.println("Commit: " + sha);
            System.out.println("Author: " + author);
            System.out.println("Message: " + message);
            System.out.println("----------------------------");
        }
    }
}