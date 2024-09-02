package io.oobeya.committracker.controller;

import io.oobeya.committracker.service.GitHubService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    @Autowired
    private GitHubService gitHubService;

    @GetMapping("/commits")
    public JsonNode getCommits(
            @RequestParam(name = "owner") String owner,
            @RequestParam(name = "repo") String repo,
            @RequestParam(name = "since") String since,
            @RequestParam(name = "until") String until) throws IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime sinceDate = LocalDateTime.parse(since, formatter);
        LocalDateTime untilDate = LocalDateTime.parse(until, formatter);

        return gitHubService.getCommits(owner, repo, sinceDate, untilDate);
    }
}