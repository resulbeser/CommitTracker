package io.oobeya.committracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data // Getter ve Setter metodlarını otomatik oluşturur
@AllArgsConstructor // Tüm argümanlara sahip constructor oluşturur
public class CommitsRequest {
    private String owner;
    private String repo;
}