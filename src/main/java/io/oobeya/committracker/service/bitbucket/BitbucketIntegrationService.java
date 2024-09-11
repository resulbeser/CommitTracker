package io.oobeya.committracker.service.bitbucket;

import io.oobeya.committracker.service.VCSIntegrationService;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BitbucketIntegrationService implements VCSIntegrationService {

    private final String accessToken;

    public BitbucketIntegrationService(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String fetchCommits(String owner, String repo, String accessToken) {
        String url = String.format("https://api.bitbucket.org/2.0/repositories/%s/%s/commits", owner, repo);
        return makeApiCall(url, this.accessToken);
    }

    @Override
    public String fetchCommitDetails(String owner, String repo, String sha, String accessToken) {
        String url = String.format("https://api.bitbucket.org/2.0/repositories/%s/%s/commit/%s", owner, repo, sha);
        return makeApiCall(url, this.accessToken);
    }

    private String makeApiCall(String url, String accessToken) {
        StringBuilder result = new StringBuilder();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            if (accessToken != null && !accessToken.isEmpty()) {
                httpGet.addHeader("Authorization", "Bearer " + accessToken);
            }

            try (CloseableHttpResponse response = httpClient.execute(httpGet);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }
        } catch (Exception e) {
            System.out.println("HATA: Bitbucket API çağrısı sırasında bir sorun oluştu: " + e.getMessage());
        }
        return result.toString();
    }
}
