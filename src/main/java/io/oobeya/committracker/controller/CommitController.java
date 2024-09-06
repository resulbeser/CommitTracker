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

            // Sadece GitHubService için commit detaylarını al
            if (vcsService instanceof GitHubService) {
                GitHubService gitHubService = (GitHubService) vcsService;
                JsonNode commitDetails = gitHubService.getCommitDetails(owner, repo, sha);

                if (commitDetails != null && commitDetails.has("files")) {
                    System.out.println("Değişiklikler:");
                    for (JsonNode file : commitDetails.get("files")) {
                        String fileName = file.has("filename") ? file.get("filename").asText() : "Unknown file";
                        int additions = file.has("additions") ? file.get("additions").asInt() : 0;
                        int deletions = file.has("deletions") ? file.get("deletions").asInt() : 0;
                        System.out.println("Dosya: " + fileName + ", Eklenen Satırlar: " + additions + ", Silinen Satırlar: " + deletions);
                    }
                }
            }

            System.out.println("----------------------------");
        }
    }
}