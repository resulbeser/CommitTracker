package io.oobeya.committracker.service;

import io.oobeya.committracker.dto.CommitResponse;
import io.oobeya.committracker.dto.CommitsRequest;

import java.util.List;

public interface CommitService {
    List<CommitResponse> getCommits(CommitsRequest request);
    CommitResponse getCommitDetails(String owner, String repo, String sha);


    // Yeni metod: Commit bilgilerini kaydetmek için
    List<CommitResponse> saveCommits(List<CommitResponse> commitResponses);
}