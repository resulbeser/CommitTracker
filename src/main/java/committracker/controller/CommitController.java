package committracker.controller;

import committracker.dto.CommitsRequest;
import committracker.dto.CommitResponse;
import committracker.enums.CommitStatus;
import committracker.service.VCSService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Secure REST Controller for handling commit-related API requests
 */
@RestController
public class CommitController {

    private final VCSService vcsService;
    
    // Security: Input validation patterns
    private static final Pattern VALID_REPO_NAME = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern VALID_OWNER_NAME = Pattern.compile("^[a-zA-Z0-9._-]+$");

    @Autowired
    public CommitController(VCSService vcsService) {
        this.vcsService = vcsService;
    }

    /**
     * Retrieves commits for a given repository with security validations
     * @param owner Repository owner/username
     * @param repo Repository name
     * @return List of formatted commit responses
     */
    @GetMapping("/commits")
    public List<CommitResponse> getCommits(@RequestParam String owner, @RequestParam String repo) {
        // Security: Input validation
        if (!isValidInput(owner, repo)) {
            System.err.println("Invalid input parameters detected in REST API");
            return new ArrayList<>();
        }
        
        CommitsRequest request = new CommitsRequest(owner, repo);
        List<JsonNode> commits = vcsService.getCommits(request);

        return Optional.ofNullable(commits)
                .map(commitList -> formatCommits(commitList, owner, repo))
                .orElseGet(() -> {
                    System.out.println("Error: Could not retrieve commit list, please check your API call.");
                    return new ArrayList<>();
                });
    }

    /**
     * Formats raw JSON commit data into CommitResponse objects
     * @param commits Raw JSON commit data
     * @param owner Repository owner
     * @param repo Repository name
     * @return List of formatted CommitResponse objects
     */
    public List<CommitResponse> formatCommits(List<JsonNode> commits, String owner, String repo) {
        List<CommitResponse> responses = new ArrayList<>();

        for (JsonNode commit : commits) {
            String sha = getValueOrDefault(commit, "sha", CommitStatus.UNKNOWN.getMessage());
            String message = getValueOrDefault(commit.at("/commit/message"), CommitStatus.NO_MESSAGE.getMessage());
            String author = getValueOrDefault(commit.at("/commit/author/name"), CommitStatus.UNKNOWN_AUTHOR.getMessage());
            String date = getValueOrDefault(commit.at("/commit/author/date"), CommitStatus.UNKNOWN_DATE.getMessage());

            // Security: Sanitize message content to prevent XSS
            message = sanitizeString(message);
            author = sanitizeString(author);

            CommitResponse commitResponse = new CommitResponse(sha, message, author, date);

            vcsService.formatCommitDetails(owner, repo, sha, commitResponse);
            responses.add(commitResponse);
        }
        return responses;
    }

    /**
     * Security: Input validation for REST API parameters
     */
    private boolean isValidInput(String owner, String repo) {
        if (owner == null || repo == null) {
            return false;
        }
        
        // Trim whitespace
        owner = owner.trim();
        repo = repo.trim();
        
        // Check for empty strings
        if (owner.isEmpty() || repo.isEmpty()) {
            return false;
        }
        
        // Check length limits
        if (owner.length() > 39 || repo.length() > 100) {
            return false;
        }
        
        // Check for valid characters only
        if (!VALID_OWNER_NAME.matcher(owner).matches() || !VALID_REPO_NAME.matcher(repo).matches()) {
            return false;
        }
        
        // Check for path traversal attempts
        if (owner.contains("..") || repo.contains("..") || 
            owner.contains("/") || repo.contains("/") ||
            owner.contains("\\") || repo.contains("\\")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Security: Sanitize strings to prevent XSS and injection attacks
     */
    private String sanitizeString(String input) {
        if (input == null) {
            return "";
        }
        
        // Remove potentially dangerous characters
        return input.replaceAll("[<>\"'&]", "")
                   .replaceAll("javascript:", "")
                   .replaceAll("data:", "")
                   .trim();
    }

    private String getValueOrDefault(JsonNode node, String defaultValue) {
        return node != null && !node.isMissingNode() ? sanitizeString(node.asText()) : defaultValue;
    }

    private String getValueOrDefault(JsonNode node, String key, String defaultValue) {
        return node.has(key) ? sanitizeString(node.get(key).asText()) : defaultValue;
    }
}
