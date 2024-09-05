package io.oobeya.committracker.controller;

import io.oobeya.committracker.dto.CommitsRequest;
import io.oobeya.committracker.service.GitHubService;
import io.oobeya.committracker.service.VCSService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class CommitController {
    private final VCSService vcsService;


    public CommitController(VCSService vcsService) {
        this.vcsService = vcsService;
    }

    public void displayCommits(String owner, String repo) {
        // Kullanıcıdan alınan owner ve repo bilgilerini kullanarak istek nesnesi oluştur
        CommitsRequest request = new CommitsRequest(owner, repo);

        // VCS servisinden commit bilgilerini al
        List<JsonNode> commits = vcsService.getCommits(request);

        // Commit bilgilerini konsola yazdır
        for (JsonNode commit : commits) {
            String sha = commit.has("sha") ? commit.get("sha").asText() : "Unknown";
            String message = commit.has("commit") && commit.get("commit").has("message")
                    ? commit.get("commit").get("message").asText()
                    : "No message";
            String author = commit.has("commit") && commit.get("commit").has("author") && commit.get("commit").get("author").has("name")
                    ? commit.get("commit").get("author").get("name").asText()
                    : "Unknown author";

            System.out.println("Commit: " + sha);
            System.out.println("Author: " + author);
            System.out.println("Message: " + message);
            System.out.println("----------------------------");
        }
    }
}