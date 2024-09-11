package io.oobeya.committracker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
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
    }

    public LocalDateTime getDateAsLocalDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return LocalDateTime.parse(this.date, formatter);
    }
}
