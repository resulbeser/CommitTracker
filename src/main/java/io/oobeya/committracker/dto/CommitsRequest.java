package io.oobeya.committracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommitsRequest {
    private String owner;
    private String repo;
}