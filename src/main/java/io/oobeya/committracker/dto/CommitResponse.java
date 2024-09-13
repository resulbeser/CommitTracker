package io.oobeya.committracker.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "commits")
public class CommitResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String sha;
    public String message;
    public String author;
    public String date;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "commit")
    public List<CommitFileResponse> files = new ArrayList<>();

    public CommitResponse(String sha, String message, String author, String date) {
        this.sha = sha;
        this.message = message;
        this.author = author;
        this.date = date;
    }
}
