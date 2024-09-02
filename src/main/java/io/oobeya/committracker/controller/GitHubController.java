package io.oobeya.committracker.controller;

import io.oobeya.committracker.service.GitHubService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.oobeya.committracker.dto.CommitsRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    @Autowired
    private GitHubService gitHubService;

    @PostMapping("/commits")
    public JsonNode getCommits(@RequestBody CommitsRequest request) throws IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime sinceDate = LocalDateTime.parse(request.getSince(), formatter);
        LocalDateTime untilDate = LocalDateTime.parse(request.getUntil(), formatter);

        return gitHubService.getCommits(request.getOwner(), request.getRepo(), sinceDate, untilDate);
    }
}