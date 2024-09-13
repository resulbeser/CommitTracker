package io.oobeya.committracker.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Entity
@Table(name = "commit_files")
public class CommitFileResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String fileName;
    public int additions;
    public int deletions;

    @ManyToOne
    @JoinColumn(name = "commit_id")
    public CommitResponse commit;

    public CommitFileResponse(String fileName, int additions, int deletions) {
        this.fileName = fileName;
        this.additions = additions;
        this.deletions = deletions;
    }
}
