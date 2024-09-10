package io.oobeya.committracker.service;

public interface VCSIntegrationService {
    String fetchCommits(String owner, String repo, String accessToken);
    String fetchCommitDetails(String owner, String repo, String sha, String accessToken);
}
