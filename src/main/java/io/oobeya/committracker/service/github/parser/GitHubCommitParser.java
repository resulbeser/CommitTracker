package io.oobeya.committracker.service.github.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.oobeya.committracker.dto.CommitFileResponse;
import io.oobeya.committracker.dto.CommitResponse;
import io.oobeya.committracker.service.CommitParserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

            // Tarihi ISO-8601 formatında ayrıştır
            LocalDateTime commitDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);

            CommitResponse commitResponse = new CommitResponse(sha, message, author, date);

            // Dosya değişikliklerini ekle
            if (commitNode.has("files")) {
                List<CommitFileResponse> files = new ArrayList<>();
                for (JsonNode fileNode : commitNode.get("files")) {
                    String fileName = fileNode.path("filename").asText();
                    int additions = fileNode.path("additions").asInt();
                    int deletions = fileNode.path("deletions").asInt();
                    files.add(new CommitFileResponse(fileName, additions, deletions));
                }
                commitResponse.setFiles(files);
            }

            return commitResponse;
        } catch (Exception e) {
            System.out.println("HATA: Commit detaylarını işlerken bir sorun oluştu: " + e.getMessage());
        }
        return null;
    }
}
