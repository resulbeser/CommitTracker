package io.oobeya.committracker.controller;

import io.oobeya.committracker.dto.CommitsRequest;
import io.oobeya.committracker.dto.CommitResponse;
import io.oobeya.committracker.enums.CommitStatus;
import io.oobeya.committracker.service.VCSService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class CommitController {

    private final VCSService vcsService;

    @Autowired
    public CommitController(VCSService vcsService) {
        this.vcsService = vcsService;
    }

    @GetMapping("/commits")
    public List<CommitResponse> getCommits(@RequestParam String owner, @RequestParam String repo) {
        CommitsRequest request = new CommitsRequest(owner, repo);
        List<JsonNode> commits = vcsService.getCommits(request);

        return Optional.ofNullable(vcsService.getCommits(request))
                .map(commitList -> formatCommits(commitList, owner, repo))
                .orElseGet(() -> {
                    System.out.println("Hata: Commit listesi alınamadı, lütfen API çağrınızı kontrol edin.");
                    return new ArrayList<>();
                });
    }

    public List<CommitResponse> formatCommits(List<JsonNode> commits, String owner, String repo) {
        List<CommitResponse> responses = new ArrayList<>();

        for (JsonNode commit : commits) {
            String sha = getValueOrDefault(commit, "sha", CommitStatus.UNKNOWN.getMessage());
            String message = getValueOrDefault(commit.at("/commit/message"), CommitStatus.NO_MESSAGE.getMessage());
            String author = getValueOrDefault(commit.at("/commit/author/name"), CommitStatus.UNKNOWN_AUTHOR.getMessage());
            String date = getValueOrDefault(commit.at("/commit/author/date"), CommitStatus.UNKNOWN_DATE.getMessage());

            CommitResponse commitResponse = new CommitResponse(sha, message, author, date);

            vcsService.formatCommitDetails(owner, repo, sha, commitResponse);
            responses.add(commitResponse);
        }
        return responses;
    }

    private String getValueOrDefault(JsonNode node, String defaultValue) {
        return node != null && !node.isMissingNode() ? node.asText() : defaultValue;
    }

    private String getValueOrDefault(JsonNode node, String key, String defaultValue) {
        return node.has(key) ? node.get(key).asText() : defaultValue;
    }
}
