package io.oobeya.committracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data // Otomatik olarak getter, setter, toString, hashCode ve equals metodlarını oluşturur
@AllArgsConstructor // Tüm alanları kapsayan bir constructor oluşturur
public class CommitFileResponse {
    private String fileName;
    private int additions;
    private int deletions;
}