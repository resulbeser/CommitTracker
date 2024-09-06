package io.oobeya.committracker.controller;

import io.oobeya.committracker.dto.CommitsRequest;
import io.oobeya.committracker.service.GitHubService;
import io.oobeya.committracker.service.VCSService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CommitController {

    @Autowired
    private final VCSService vcsService;


    public CommitController(VCSService vcsService) {
        this.vcsService = vcsService;
    }

    public void displayCommits(String owner, String repo) {
        // Kullanıcıdan alınan owner ve repo bilgilerini kullanarak istek nesnesi oluştur
        CommitsRequest request = new CommitsRequest(owner, repo);

        // VCS servisinden commit bilgilerini al
        List<JsonNode> commits = vcsService.getCommits(request);

        // Commit bilgilerini formatlı bir şekilde konsola yazdır
        for (JsonNode commit : commits) {
            String sha = commit.has("sha") ? commit.get("sha").asText() : "Unknown";
            String message = commit.has("commit") && commit.get("commit").has("message")
                    ? commit.get("commit").get("message").asText()
                    : "No message";
            String author = commit.has("commit") && commit.get("commit").has("author") && commit.get("commit").get("author").has("name")
                    ? commit.get("commit").get("author").get("name").asText()
                    : "Unknown author";
            String date = commit.has("commit") && commit.get("commit").has("author") && commit.get("commit").get("author").has("date")
                    ? commit.get("commit").get("author").get("date").asText()
                    : "Unknown date";

            // Çıktı formatı
            System.out.println("========================================");
            System.out.println("Commit SHA: " + sha);
            System.out.println("Author: " + author);
            System.out.println("Date: " + date);
            System.out.println("Message: " + message);

            // Sadece GitHubService için commit detaylarını al ve göster
            if (vcsService instanceof GitHubService) {
                GitHubService gitHubService = (GitHubService) vcsService;
                JsonNode commitDetails = gitHubService.getCommitDetails(owner, repo, sha);

                if (commitDetails != null && commitDetails.has("files")) {
                    System.out.println("\nChanged Files:");
                    int fileIndex = 1;
                    for (JsonNode file : commitDetails.get("files")) {
                        String fileName = file.has("filename") ? file.get("filename").asText() : "Unknown file";
                        int additions = file.has("additions") ? file.get("additions").asInt() : 0;
                        int deletions = file.has("deletions") ? file.get("deletions").asInt() : 0;

                        System.out.println(fileIndex++ + ". File: " + fileName);
                        System.out.println("   - Added Lines: " + additions);
                        System.out.println("   - Deleted Lines: " + deletions);
                    }
                }
            }
            System.out.println("----------------------------------------\n");
        }
    }
}

//if for vs ekleme controllerde olmaz
//constant yapısı ve enum yapısı
//controller advice controller altında
//commit response oluştur sistym outta.