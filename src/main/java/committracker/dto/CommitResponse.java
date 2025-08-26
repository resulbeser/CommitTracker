package committracker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for commit response data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitResponse {
    private String sha;
    private String message;
    private String authorName;
    private String authorEmail;
    private String committerName;
    private String committerEmail;
    private String date;
    private int additions;
    private int deletions;
    private int totalChanges;
    private String url;
    private String htmlUrl;
    private List<CommitFileResponse> files;
    
    // Constructor for basic usage
    public CommitResponse(String sha, String message, String authorName, String date) {
        this.sha = sha;
        this.message = message;
        this.authorName = authorName;
        this.date = date;
    }
    
    // Helper method for backward compatibility
    public String getAuthor() {
        return authorName;
    }
}
