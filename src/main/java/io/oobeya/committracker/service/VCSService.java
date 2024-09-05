package io.oobeya.committracker.service;

import io.oobeya.committracker.dto.CommitsRequest;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface VCSService {
    List<JsonNode> getCommits(CommitsRequest request);
}