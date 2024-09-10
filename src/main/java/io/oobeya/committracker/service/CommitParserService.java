package io.oobeya.committracker.service;

import io.oobeya.committracker.dto.CommitResponse;

import java.util.List;

public interface CommitParserService {
    List<CommitResponse> parseCommits(String jsonResponse);
    CommitResponse parseCommitDetails(String jsonResponse);
}