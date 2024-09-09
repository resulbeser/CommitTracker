package io.oobeya.committracker.service;

import io.oobeya.committracker.dto.CommitResponse;
import io.oobeya.committracker.dto.CommitsRequest;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface VCSService {
    //TODO : JSONnode yerine belirli bir response dönelim, Commit response gibi bir responseun olsun
    List<JsonNode> getCommits(CommitsRequest request);
    JsonNode getCommitDetails(String owner, String repo, String sha);

    void formatCommitDetails(String owner, String repo, String sha, CommitResponse commitResponse);

}