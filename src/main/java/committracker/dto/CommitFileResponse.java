package committracker.dto;
import lombok.Data;
/**
 * Data Transfer Object for commit file changes
 */
@Data
public class CommitFileResponse {
    private String filename;
    private String status;
    private int additions;
    private int deletions;
    private int changes;
    private String patch;
    private String blobUrl;
    private String rawUrl;
    
    // Helper method for backward compatibility
    public String getFileName() {
        return filename;
    }
}

