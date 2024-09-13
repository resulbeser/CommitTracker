package io.oobeya.committracker.service.github;

import io.oobeya.committracker.dto.CommitResponse;
import io.oobeya.committracker.dto.CommitsRequest;
import io.oobeya.committracker.service.CommitParserService;
import io.oobeya.committracker.service.CommitService;
import io.oobeya.committracker.service.VCSIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubCommitService implements CommitService {

    public final VCSIntegrationService integrationService;
    public final CommitParserService parserService;
    public final List<CommitResponse> commitStorage = new ArrayList<>(); // In-memory storage

    @Autowired
    public GitHubCommitService(VCSIntegrationService integrationService, CommitParserService parserService) {
        this.integrationService = integrationService;
        this.parserService = parserService;
    }

    @Override
    public List<CommitResponse> getCommits(CommitsRequest request) {
        String jsonResponse = integrationService.fetchCommits(request.getOwner(), request.getRepo(), null);
        return parserService.parseCommits(jsonResponse);
    }

    @Override
    public CommitResponse getCommitDetails(String owner, String repo, String sha) {
        String jsonResponse = integrationService.fetchCommitDetails(owner, repo, sha, null);
        return parserService.parseCommitDetails(jsonResponse);
    }

    @Override
    public List<CommitResponse> saveCommits(List<CommitResponse> commitResponses) {
        commitStorage.addAll(commitResponses);
        return commitResponses;
    }

    public List<CommitResponse> getStoredCommits() {
        return new ArrayList<>(commitStorage);
    }
}