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

    private final String accessToken;

    public GitHubService(String accessToken) {

        this.accessToken = accessToken;
    }


    public List<JsonNode> getCommits(CommitsRequest request) {
        List<JsonNode> commits = new ArrayList<>();
        String url = String.format("https://api.github.com/repos/%s/%s/commits", request.getOwner(), request.getRepo());

        System.out.println("Request URL: " + url);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Authorization", "Bearer " + accessToken);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                System.out.println("Response Status Code: " + statusCode);

                if (statusCode == 404) {
                    System.out.println("Repo bulunamadı. Kullanıcı adı ve repo adını kontrol edin.");
                    return commits;
                } else if (statusCode == 401) {
                    System.out.println("Yetkisiz erişim. Token geçersiz veya izinsiz.");
                    return commits;
                } else if (statusCode != 200) {
                    System.out.println("API hatası: " + statusCode);
                    return commits;
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.getEntity().getContent());

                System.out.println("API Yanıtı: " + jsonResponse.toPrettyString());
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