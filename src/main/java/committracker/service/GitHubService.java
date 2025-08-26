package committracker.service;

import committracker.dto.CommitResponse;
import committracker.dto.CommitsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Secure GitHub API service implementation for fetching commit data
 */
@Service
public class GitHubService implements VCSService {
    private final String accessToken;

    // Security: Rate limiting per API endpoint
    private final ConcurrentHashMap<String, AtomicInteger> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    // Security: Input validation patterns
    private static final Pattern VALID_REPO_NAME = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern VALID_OWNER_NAME = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern VALID_SHA = Pattern.compile("^[a-fA-F0-9]{40}$");

    public GitHubService(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public List<JsonNode> getCommits(CommitsRequest request) {
        List<JsonNode> commits = new ArrayList<>();

        // Security: Input validation to prevent path traversal and injection attacks
        if (!isValidInput(request.getOwner(), request.getRepo())) {
            System.err.println("Invalid input parameters detected");
            return commits;
        }

        try (CloseableHttpClient httpClient = createSecureHttpClient()) {
            String baseUrl = "https://api.github.com/repos/" +
                URLEncoder.encode(request.getOwner(), StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(request.getRepo(), StandardCharsets.UTF_8) + "/commits";

            // Security: Rate limiting check
            if (!checkRateLimit(baseUrl)) {
                System.err.println("Rate limit exceeded for GitHub API");
                return commits;
            }

            HttpGet httpGet = new HttpGet(baseUrl);

            // Add authorization header if token is provided
            if (accessToken != null && !accessToken.isEmpty()) {
                httpGet.setHeader("Authorization", "token " + accessToken);
            }

            httpGet.setHeader("Accept", "application/vnd.github.v3+json");
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
                    System.err.println("Repository not found or is private. Please check repository name and access token.");
                } else if (response.getCode() == 403) {
                    System.err.println("Access denied. Rate limit exceeded or invalid token.");
                } else {
                    System.err.println("GitHub API Error: " + response.getCode() + " - " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching commits from GitHub: " + e.getMessage());
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
            String url = "https://api.github.com/repos/" +
                URLEncoder.encode(owner, StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/commits/" + sha;

            // Security: Rate limiting check
            if (!checkRateLimit(url)) {
                System.err.println("Rate limit exceeded for GitHub API");
                return null;
            }

            HttpGet httpGet = new HttpGet(url);

            if (accessToken != null && !accessToken.isEmpty()) {
                httpGet.setHeader("Authorization", "token " + accessToken);
            }

            httpGet.setHeader("Accept", "application/vnd.github.v3+json");
            httpGet.setHeader("User-Agent", "CommitTracker-App");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                if (response.getCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readTree(responseBody);
                } else {
                    System.err.println("GitHub API Error: " + response.getCode() + " - " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching commit details from GitHub: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void formatCommitDetails(String owner, String repo, String sha, CommitResponse commitResponse) {
        JsonNode commitDetails = getCommitDetails(owner, repo, sha);

        if (commitDetails != null) {
            JsonNode commit = commitDetails.get("commit");
            if (commit != null) {
                JsonNode author = commit.get("author");
                JsonNode committer = commit.get("committer");

                if (author != null) {
                    commitResponse.setAuthorName(author.get("name").asText());
                    commitResponse.setAuthorEmail(author.get("email").asText());
                    commitResponse.setDate(author.get("date").asText());
                }

                if (committer != null) {
                    commitResponse.setCommitterName(committer.get("name").asText());
                    commitResponse.setCommitterEmail(committer.get("email").asText());
                }

                commitResponse.setMessage(commit.get("message").asText());
            }

            JsonNode stats = commitDetails.get("stats");
            if (stats != null) {
                commitResponse.setAdditions(stats.get("additions").asInt());
                commitResponse.setDeletions(stats.get("deletions").asInt());
                commitResponse.setTotalChanges(stats.get("total").asInt());
            }
        }
    }

    /**
     * Security: Validate input parameters to prevent injection attacks
     */
    private boolean isValidInput(String owner, String repo) {
        if (owner == null || repo == null) {
            return false;
        }

        // Check length limits
        if (owner.length() > 39 || repo.length() > 100) {
            return false;
        }

        // Validate against patterns
        return VALID_OWNER_NAME.matcher(owner).matches() &&
               VALID_REPO_NAME.matcher(repo).matches();
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
     * Security: Create HTTP client with proper SSL/TLS configuration and timeouts
     */
    private CloseableHttpClient createSecureHttpClient() throws Exception {
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(10))
                .setResponseTimeout(Timeout.ofSeconds(30))
                .build();
        
        return HttpClients.custom()
                .useSystemProperties()
                .setDefaultRequestConfig(config)
                .build();
    }
}
