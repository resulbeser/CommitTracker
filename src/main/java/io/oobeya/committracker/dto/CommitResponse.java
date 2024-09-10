package io.oobeya.committracker.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data // Getter, Setter, toString, hashCode ve equals metodlarını otomatik olarak oluşturur
@NoArgsConstructor // Parametresiz bir constructor oluşturur
public class CommitResponse {
    private String sha;
    private String message;
    private String author;
    private String date;
    private List<CommitFileResponse> files = new ArrayList<>();

    public CommitResponse(String sha, String message, String author, String date) {
        this.sha = sha;
        this.message = message;
        this.author = author;
        this.date = date;
        this.files = new ArrayList<>();
    }

    public void setFiles(JsonNode fileNodes) {
        for (JsonNode fileNode : fileNodes) {
            String fileName = fileNode.has("filename") ? fileNode.get("filename").asText() : "Unknown file";
            int additions = fileNode.has("additions") ? fileNode.get("additions").asInt() : 0;
            int deletions = fileNode.has("deletions") ? fileNode.get("deletions").asInt() : 0;
            files.add(new CommitFileResponse(fileName, additions, deletions));
        }
    }
}
