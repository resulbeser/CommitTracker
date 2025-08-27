package committracker.service;

import committracker.dto.CommitResponse;
import committracker.dto.CommitsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Bitbucket API service implementation for fetching commit data
 */
public class BitbucketService implements VCSService {
    private final String accessToken;

    // Security: Rate limiting per API endpoint
    private final ConcurrentHashMap<String, AtomicInteger> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    // Security: Input validation patterns
    private static final Pattern VALID_WORKSPACE = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern VALID_REPO_NAME = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern VALID_SHA = Pattern.compile("^[a-fA-F0-9]{40}$");

    public BitbucketService(String accessToken) {
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
            String baseUrl = "https://api.bitbucket.org/2.0/repositories/" +
                URLEncoder.encode(request.getOwner(), StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(request.getRepo(), StandardCharsets.UTF_8) + "/commits";

            // Security: Rate limiting check
            if (!checkRateLimit(baseUrl)) {
                System.err.println("Rate limit exceeded for Bitbucket API");
                return commits;
            }

            HttpGet httpGet = new HttpGet(baseUrl);

            if (accessToken != null && !accessToken.isEmpty()) {
                String auth = Base64.getEncoder().encodeToString(("x-token-auth:" + accessToken).getBytes());
                httpGet.setHeader("Authorization", "Basic " + auth);
            }

            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("User-Agent", "CommitTracker-App");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                if (response.getCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(responseBody);

                    JsonNode values = rootNode.get("values");
                    if (values != null && values.isArray()) {
                        for (JsonNode commit : values) {
                            commits.add(commit);
                        }
                    }
                } else if (response.getCode() == 404) {
                    System.err.println("Bitbucket repository not found or is private. Please check repository name and access token.");
                } else if (response.getCode() == 403) {
                    System.err.println("Access denied. Invalid token or insufficient permissions.");
                } else {
                    System.err.println("Bitbucket API Error: " + response.getCode() + " - " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching commits from Bitbucket: " + e.getMessage());
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
            String url = "https://api.bitbucket.org/2.0/repositories/" +
                URLEncoder.encode(owner, StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(repo, StandardCharsets.UTF_8) + "/commits/" + sha;

            // Security: Rate limiting check
            if (!checkRateLimit(url)) {
                System.err.println("Rate limit exceeded for Bitbucket API");
                return null;
            }

            HttpGet httpGet = new HttpGet(url);

            if (accessToken != null && !accessToken.isEmpty()) {
                String auth = Base64.getEncoder().encodeToString(("x-token-auth:" + accessToken).getBytes());
                httpGet.setHeader("Authorization", "Basic " + auth);
            }

            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("User-Agent", "CommitTracker-App");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                if (response.getCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readTree(responseBody);
                } else {
                    System.err.println("Bitbucket API Error: " + response.getCode() + " - " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching commit details from Bitbucket: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void formatCommitDetails(String owner, String repo, String sha, CommitResponse commitResponse) {
        JsonNode commitDetails = getCommitDetails(owner, repo, sha);

        if (commitDetails != null) {
            JsonNode author = commitDetails.get("author");
            if (author != null) {
                commitResponse.setAuthorName(author.get("display_name").asText());
                JsonNode user = author.get("user");
                if (user != null) {
                    commitResponse.setAuthorEmail(user.get("email").asText());
                }
            }

            commitResponse.setDate(commitDetails.get("date").asText());
            commitResponse.setMessage(commitDetails.get("message").asText());

            // Bitbucket doesn't provide detailed stats in the commit endpoint
            // Would need to fetch diff separately for line counts
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

        return VALID_WORKSPACE.matcher(owner).matches() &&
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
     * Security: Create HTTP client with proper SSL/TLS configuration
     */
    private CloseableHttpClient createSecureHttpClient() throws Exception {
        return HttpClients.custom()
                .useSystemProperties()
                .build();
    }
}
