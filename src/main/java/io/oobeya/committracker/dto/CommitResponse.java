package io.oobeya.committracker.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class CommitResponse {
    private String sha;
    private String message;
    private String author;
    private String date;
    private List<CommitFileResponse> files;

    public CommitResponse(String sha, String message, String author, String date) {
        this.sha = sha;
        this.message = message;
        this.author = author;
        this.date = date;
        this.files = new ArrayList<>();
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<CommitFileResponse> getFiles() {
        return files;
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
