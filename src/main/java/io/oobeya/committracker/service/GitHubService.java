package io.oobeya.committracker.service;

import io.oobeya.committracker.dto.CommitsRequest;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class GitHubService {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/resulbeser/CommitTracker/commits";
    private static final String TOKEN = "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";

    public List<JsonNode> getCommits(CommitsRequest request) {
        List<JsonNode> commits = new ArrayList<>();
        String url = String.format(GITHUB_API_URL, request.getOwner(), request.getRepo());

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Authorization", "Bearer " + TOKEN);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.getEntity().getContent());

                System.out.println(jsonResponse.toPrettyString());
                jsonResponse.forEach(commits::add);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return commits;
    }
}

/* Bu sınıf, GitHub API ile iletişim kurarak bir repository'den commit bilgilerini çeker.
getCommits metodu, belirli bir repository'den commit verilerini alır ve JSON formatında işler.
API'ye erişim sağlamak için bir Personal Access Token (PAT) kullanılır ve API yanıtı JSON
formatından bir listeye dönüştürülür.*/