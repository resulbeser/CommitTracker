package io.oobeya.committracker.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GitHubService {

    @Value("${github.token}")
    private String token;
    private static final String BASE_URL = "https://api.github.com";

    // Kullanıcı adınızı ve depo adınızı ekleyin
    private static final String owner = "resulbeser"; // Kendi GitHub kullanıcı adınızı buraya yazın
    private static final String repo = "CommitTracker"; // Çekmek istediğiniz depo adını buraya yazın

    public JsonNode getCommits(String owner, String repo, LocalDateTime since, LocalDateTime until) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        ObjectMapper objectMapper = new ObjectMapper();

        String commitsUrl = BASE_URL + "/repos/" + owner + "/" + repo + "/commits";
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String sinceParam = since.format(formatter);
        String untilParam = until.format(formatter);

        HttpGet httpGet = new HttpGet(commitsUrl + "?since=" + sinceParam + "&until=" + untilParam);
        httpGet.setHeader("Authorization", "token " + token);

        HttpResponse response = httpClient.execute(httpGet);
        String responseBody = EntityUtils.toString(response.getEntity());

        httpClient.close();
        return objectMapper.readTree(responseBody);
    }
}
