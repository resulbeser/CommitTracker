package committracker.service;

import committracker.dto.CommitResponse;
import committracker.dto.CommitsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * GitLab API service implementation for fetching commit data
 */
public class GitLabService implements VCSService {
    private final String accessToken;

    // Security: Rate limiting per API endpoint
    private final ConcurrentHashMap<String, AtomicInteger> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    // Security: Input validation patterns
    private static final Pattern VALID_PROJECT_NAME = Pattern.compile("^[a-zA-Z0-9._/-]+$");
    private static final Pattern VALID_SHA = Pattern.compile("^[a-fA-F0-9]{40}$");

    public GitLabService(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public List<JsonNode> getCommits(CommitsRequest request) {
        List<JsonNode> commits = new ArrayList<>();

        // Security: Input validation
        if (!isValidInput(request.getOwner(), request.getRepo())) {
            System.err.println("Invalid input parameters detected");
            return commits;
        }

        try (CloseableHttpClient httpClient = createSecureHttpClient()) {
            String projectPath = URLEncoder.encode(request.getOwner() + "/" + request.getRepo(), StandardCharsets.UTF_8);
            String baseUrl = "https://gitlab.com/api/v4/projects/" + projectPath + "/repository/commits";

            // Security: Rate limiting check
            if (!checkRateLimit(baseUrl)) {
                System.err.println("Rate limit exceeded for GitLab API");
                return commits;
            }

            HttpGet httpGet = new HttpGet(baseUrl);

            if (accessToken != null && !accessToken.isEmpty()) {
                httpGet.setHeader("PRIVATE-TOKEN", accessToken);
            }

            httpGet.setHeader("User-Agent", "CommitTracker-App");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                if (response.getCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(responseBody);

                    if (rootNode.isArray()) {
                        for (JsonNode commit : rootNode) {
                            commits.add(commit);
                        }
                    }
                } else if (response.getCode() == 404) {
                    System.err.println("GitLab project not found or is private. Please check project name and access token.");
                } else if (response.getCode() == 403) {
                    System.err.println("Access denied. Invalid token or insufficient permissions.");
                } else {
                    System.err.println("GitLab API Error: " + response.getCode() + " - " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching commits from GitLab: " + e.getMessage());
        }

        return commits;
    }

    @Override
    public JsonNode getCommitDetails(String owner, String repo, String sha) {
        // Security: Input validation
        if (!isValidInput(owner, repo) || !VALID_SHA.matcher(sha).matches()) {
            System.err.println("Invalid input parameters for commit details");
            return null;
        }

        try (CloseableHttpClient httpClient = createSecureHttpClient()) {
            String projectPath = URLEncoder.encode(owner + "/" + repo, StandardCharsets.UTF_8);
            String url = "https://gitlab.com/api/v4/projects/" + projectPath + "/repository/commits/" + sha;

            // Security: Rate limiting check
            if (!checkRateLimit(url)) {
                System.err.println("Rate limit exceeded for GitLab API");
                return null;
            }

            HttpGet httpGet = new HttpGet(url);

            if (accessToken != null && !accessToken.isEmpty()) {
                httpGet.setHeader("PRIVATE-TOKEN", accessToken);
            }

            httpGet.setHeader("User-Agent", "CommitTracker-App");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                if (response.getCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readTree(responseBody);
                } else {
                    System.err.println("GitLab API Error: " + response.getCode() + " - " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching commit details from GitLab: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void formatCommitDetails(String owner, String repo, String sha, CommitResponse commitResponse) {
        JsonNode commitDetails = getCommitDetails(owner, repo, sha);

        if (commitDetails != null) {
            commitResponse.setAuthorName(commitDetails.get("author_name").asText());
            commitResponse.setAuthorEmail(commitDetails.get("author_email").asText());
            commitResponse.setCommitterName(commitDetails.get("committer_name").asText());
            commitResponse.setCommitterEmail(commitDetails.get("committer_email").asText());
            commitResponse.setDate(commitDetails.get("created_at").asText());
            commitResponse.setMessage(commitDetails.get("message").asText());

            JsonNode stats = commitDetails.get("stats");
            if (stats != null) {
                commitResponse.setAdditions(stats.get("additions").asInt());
                commitResponse.setDeletions(stats.get("deletions").asInt());
                commitResponse.setTotalChanges(stats.get("additions").asInt() + stats.get("deletions").asInt());
            }
        }
    }

    /**
     * Security: Validate input parameters
     */
    private boolean isValidInput(String owner, String repo) {
        if (owner == null || repo == null) {
            return false;
        }

        // Check length limits
        if (owner.length() > 100 || repo.length() > 100) {
            return false;
        }

        String projectPath = owner + "/" + repo;
        return VALID_PROJECT_NAME.matcher(projectPath).matches();
    }

    /**
     * Security: Simple rate limiting implementation
     */
    private boolean checkRateLimit(String endpoint) {
        String key = endpoint + "_" + (System.currentTimeMillis() / 60000);
        AtomicInteger count = rateLimitMap.computeIfAbsent(key, k -> new AtomicInteger(0));

        // Clean old entries
        rateLimitMap.entrySet().removeIf(entry ->
            !entry.getKey().endsWith("_" + (System.currentTimeMillis() / 60000)));

        return count.incrementAndGet() <= MAX_REQUESTS_PER_MINUTE;
    }

    /**
     * Security: Create HTTP client with proper SSL/TLS configuration
     */
    private CloseableHttpClient createSecureHttpClient() throws Exception {
        return HttpClients.custom()
                .useSystemProperties()
                .build();
    }
}
