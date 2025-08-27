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
 * Azure DevOps API service implementation for fetching commit data
 */
public class AzureDevOpsService implements VCSService {
    private final String accessToken;

    // Security: Rate limiting per API endpoint
    private final ConcurrentHashMap<String, AtomicInteger> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    // Security: Input validation patterns
    private static final Pattern VALID_ORG_NAME = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern VALID_PROJECT_NAME = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern VALID_REPO_NAME = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern VALID_COMMIT_ID = Pattern.compile("^[a-fA-F0-9]{40}$");

    public AzureDevOpsService(String accessToken) {
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
            // Azure DevOps format: https://dev.azure.com/{organization}/{project}/_apis/git/repositories/{repositoryId}/commits
            String baseUrl = "https://dev.azure.com/" +
                URLEncoder.encode(request.getOwner(), StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(request.getRepo(), StandardCharsets.UTF_8) +
                "/_apis/git/repositories/" +
                URLEncoder.encode(request.getRepo(), StandardCharsets.UTF_8) +
                "/commits?api-version=6.0";

            // Security: Rate limiting check
            if (!checkRateLimit(baseUrl)) {
                System.err.println("Rate limit exceeded for Azure DevOps API");
                return commits;
            }

            HttpGet httpGet = new HttpGet(baseUrl);

            if (accessToken != null && !accessToken.isEmpty()) {
                String auth = Base64.getEncoder().encodeToString((":" + accessToken).getBytes());
                httpGet.setHeader("Authorization", "Basic " + auth);
            }

            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("User-Agent", "CommitTracker-App");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                if (response.getCode() == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(responseBody);

                    JsonNode values = rootNode.get("value");
                    if (values != null && values.isArray()) {
                        for (JsonNode commit : values) {
                            commits.add(commit);
                        }
                    }
                } else if (response.getCode() == 404) {
                    System.err.println("Azure DevOps repository not found or is private. Please check organization/project/repository names and access token.");
                } else if (response.getCode() == 403) {
                    System.err.println("Access denied. Invalid token or insufficient permissions.");
                } else {
                    System.err.println("Azure DevOps API Error: " + response.getCode() + " - " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching commits from Azure DevOps: " + e.getMessage());
        }

        return commits;
    }

    @Override
    public JsonNode getCommitDetails(String organization, String project, String commitId) {
        // Security: Input validation
        if (!isValidInput(organization, project) || !VALID_COMMIT_ID.matcher(commitId).matches()) {
            System.err.println("Invalid input parameters for commit details");
            return null;
        }

        try (CloseableHttpClient httpClient = createSecureHttpClient()) {
            String url = "https://dev.azure.com/" +
                URLEncoder.encode(organization, StandardCharsets.UTF_8) + "/" +
                URLEncoder.encode(project, StandardCharsets.UTF_8) +
                "/_apis/git/repositories/" +
                URLEncoder.encode(project, StandardCharsets.UTF_8) +
                "/commits/" + commitId + "?api-version=6.0";

            // Security: Rate limiting check
            if (!checkRateLimit(url)) {
                System.err.println("Rate limit exceeded for Azure DevOps API");
                return null;
            }

            HttpGet httpGet = new HttpGet(url);

            if (accessToken != null && !accessToken.isEmpty()) {
                String auth = Base64.getEncoder().encodeToString((":" + accessToken).getBytes());
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
                    System.err.println("Azure DevOps API Error: " + response.getCode() + " - " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching commit details from Azure DevOps: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void formatCommitDetails(String organization, String project, String commitId, CommitResponse commitResponse) {
        JsonNode commitDetails = getCommitDetails(organization, project, commitId);

        if (commitDetails != null) {
            JsonNode author = commitDetails.get("author");
            JsonNode committer = commitDetails.get("committer");

            if (author != null) {
                commitResponse.setAuthorName(author.get("name").asText());
                commitResponse.setAuthorEmail(author.get("email").asText());
                commitResponse.setDate(author.get("date").asText());
            }

            if (committer != null) {
                commitResponse.setCommitterName(committer.get("name").asText());
                commitResponse.setCommitterEmail(committer.get("email").asText());
            }

            commitResponse.setMessage(commitDetails.get("comment").asText());

            // Azure DevOps doesn't provide detailed stats in the commit endpoint
            // Would need to fetch changes separately for line counts
        }
    }

    /**
     * Security: Validate input parameters
     */
    private boolean isValidInput(String organization, String project) {
        if (organization == null || project == null) {
            return false;
        }

        // Check length limits
        if (organization.length() > 100 || project.length() > 100) {
            return false;
        }

        return VALID_ORG_NAME.matcher(organization).matches() &&
               VALID_PROJECT_NAME.matcher(project).matches();
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
