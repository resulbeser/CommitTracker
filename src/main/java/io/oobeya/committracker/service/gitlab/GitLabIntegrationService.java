package io.oobeya.committracker.service.gitlab;

import io.oobeya.committracker.service.VCSIntegrationService;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GitLabIntegrationService implements VCSIntegrationService {

    private final String accessToken;

    public GitLabIntegrationService(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String fetchCommits(String owner, String repo, String accessToken) {
        try {
            // Kullanıcı adı ve repo adını URL için uygun formata kodlayın
            String encodedOwner = URLEncoder.encode(owner, StandardCharsets.UTF_8.toString());
            String encodedRepo = URLEncoder.encode(repo, StandardCharsets.UTF_8.toString());

            // GitLab API URL'sini oluştur
            String url = String.format("https://gitlab.com/api/v4/projects/%s%%2F%s/repository/commits", encodedOwner, encodedRepo);
            return makeApiCall(url, this.accessToken);

        } catch (Exception e) {
            System.out.println("HATA: URL kodlaması sırasında bir sorun oluştu: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String fetchCommitDetails(String owner, String repo, String sha, String accessToken) {
        try {
            // Kullanıcı adı ve repo adını URL için uygun formata kodlayın
            String encodedOwner = URLEncoder.encode(owner, StandardCharsets.UTF_8.toString());
            String encodedRepo = URLEncoder.encode(repo, StandardCharsets.UTF_8.toString());

            // GitLab API URL'sini oluştur
            String url = String.format("https://gitlab.com/api/v4/projects/%s%%2F%s/repository/commits/%s", encodedOwner, encodedRepo, sha);
            return makeApiCall(url, this.accessToken);

        } catch (Exception e) {
            System.out.println("HATA: URL kodlaması sırasında bir sorun oluştu: " + e.getMessage());
            return null;
        }
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
            System.out.println("HATA: GitLab API çağrısı sırasında bir sorun oluştu: " + e.getMessage());
        }
        return result.toString();
    }
}
