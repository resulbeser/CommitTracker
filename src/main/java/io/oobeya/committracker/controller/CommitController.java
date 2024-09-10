package io.oobeya.committracker.controller;

import io.oobeya.committracker.dto.CommitResponse;
import io.oobeya.committracker.dto.CommitsRequest;
import io.oobeya.committracker.service.CommitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommitController {

    private final CommitService commitService;

    @Autowired
    public CommitController(CommitService commitService) {
        this.commitService = commitService;
    }

    @GetMapping("/commits")
    public List<CommitResponse> getCommits(@RequestParam String owner, @RequestParam String repo) {
        CommitsRequest request = new CommitsRequest(owner, repo);
        List<CommitResponse> commitResponses = commitService.getCommits(request);
        commitService.saveCommits(commitResponses); // Commit'leri kaydet
        return commitResponses;
    }
}
