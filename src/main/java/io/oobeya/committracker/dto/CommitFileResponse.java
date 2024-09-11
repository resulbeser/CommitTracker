package io.oobeya.committracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommitFileResponse {
    private String fileName;
    private int additions;
    private int deletions;
}
