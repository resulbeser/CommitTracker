package committracker.service;

import committracker.dto.CommitResponse;
import committracker.dto.CommitsRequest;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface VCSService {
    List<JsonNode> getCommits(CommitsRequest request);
    JsonNode getCommitDetails(String owner, String repo, String sha);
    void formatCommitDetails(String owner, String repo, String sha, CommitResponse commitResponse);
}
