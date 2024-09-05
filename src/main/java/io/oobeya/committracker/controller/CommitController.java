package io.oobeya.committracker.controller;

import io.oobeya.committracker.dto.CommitsRequest;
import io.oobeya.committracker.service.GitHubService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;



public class CommitController {
    private final GitHubService gitHubService;


    public CommitController(String accessToken) {
        // Kullanıcı adı ve token bilgilerini GitHubService'e geç
        this.gitHubService = new GitHubService(accessToken);
    }

    public void displayCommits(String owner, String repo) {
        CommitsRequest request = new CommitsRequest(owner, repo);
        List<JsonNode> commits = gitHubService.getCommits(request);

        for (JsonNode commit : commits) {
            String sha = commit.get("sha") != null ? commit.get("sha").asText() : "Unknown";
            String message = commit.get("commit") != null && commit.get("commit").get("message") != null
                    ? commit.get("commit").get("message").asText()
                    : "No message";
            String author = commit.get("commit") != null && commit.get("commit").get("author") != null
                    && commit.get("commit").get("author").get("name") != null
                    ? commit.get("commit").get("author").get("name").asText()
                    : "Unknown author";

            System.out.println("Commit: " + sha);
            System.out.println("Author: " + author);
            System.out.println("Message: " + message);
            System.out.println("----------------------------");
        }
    }
}