package io.oobeya.committracker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "commit")
@Data
public class Commit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sha;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private LocalDateTime date;

    @OneToMany(mappedBy = "commit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommitFile> files;
}
