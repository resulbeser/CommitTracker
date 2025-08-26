package committracker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Data Transfer Object for commit requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitsRequest {
    private String platform;
    private String owner;
    private String repo;
    private String accessToken;
    private String branch;
    private int limit = 30; // Default limit
    
    // Constructor for basic usage
    public CommitsRequest(String owner, String repo) {
        this.owner = owner;
        this.repo = repo;
    }
}
