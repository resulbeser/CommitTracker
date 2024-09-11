package io.oobeya.committracker.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "commit_file")
@Data
public class CommitFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "commit_id", nullable = false)
    private Commit commit;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "additions")
    private int additions;

    @Column(name = "deletions")
    private int deletions;
}
