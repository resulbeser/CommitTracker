package io.oobeya.committracker.service.github.parser;

import io.oobeya.committracker.dto.CommitResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.oobeya.committracker.service.CommitParserService;

import java.util.ArrayList;
import java.util.List;

public class GitHubCommitParser implements CommitParserService {

    @Override
    public List<CommitResponse> parseCommits(String jsonResponse) {
        List<CommitResponse> commitResponses = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            for (JsonNode commitNode : rootNode) {
                commitResponses.add(parseCommitDetails(commitNode.toString()));
            }
        } catch (Exception e) {
            System.out.println("HATA: Commitleri işlerken bir sorun oluştu: " + e.getMessage());
        }
        return commitResponses;
    }

    @Override
    public CommitResponse parseCommitDetails(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode commitNode = objectMapper.readTree(jsonResponse);
            String sha = commitNode.path("sha").asText();
            String message = commitNode.path("commit").path("message").asText();
            String author = commitNode.path("commit").path("author").path("name").asText();
            String date = commitNode.path("commit").path("author").path("date").asText();
            CommitResponse commitResponse = new CommitResponse(sha, message, author, date);

            // Dosya değişikliklerini ekle
            if (commitNode.has("files")) {
                commitResponse.setFiles(commitNode.get("files"));
            }

            return commitResponse;
        } catch (Exception e) {
            System.out.println("HATA: Commit detaylarını işlerken bir sorun oluştu: " + e.getMessage());
        }
        return null;
    }
}
