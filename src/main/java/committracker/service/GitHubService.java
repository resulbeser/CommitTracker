package committracker.service;

import committracker.dto.CommitsRequest;
import committracker.dto.CommitResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubService implements VCSService {
    private final String accessToken;

    public GitHubService(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public List<JsonNode> getCommits(CommitsRequest request) {
        List<JsonNode> commits = new ArrayList<>();
        String url = String.format("https://api.github.com/repos/%s/%s/commits", request.getOwner(), request.getRepo());

        System.out.println("\n========================================");
        System.out.println("=> GitHub API İsteği Gönderiliyor: ");
        System.out.println("   URL: " + url);
        System.out.println("========================================");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            if (accessToken != null && !accessToken.isEmpty()) {
                httpGet.addHeader("Authorization", "Bearer " + accessToken);
            }

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                System.out.println("=> Yanıt Durum Kodu: " + statusCode);

                if (statusCode == 404) {
                    System.out.println("HATA: Repo bulunamadı. Bu repo özel olabilir veya kullanıcı adı/repo adı hatalı olabilir.");
                    return commits;
                } else if (statusCode == 401) {
                    System.out.println("HATA: Yetkisiz erişim. Eğer bu repo özelse, geçerli bir access token girmeniz gerekiyor.");
                    return commits;
                } else if (statusCode != 200) {
                    System.out.println("HATA: API hatası (Durum Kodu: " + statusCode + ")");
                    return commits;
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.getEntity().getContent());

                System.out.println("=> API Yanıtı Başarıyla Alındı. Commitler Listeleniyor...");
                System.out.println("========================================");

                jsonResponse.forEach(commit -> {
                    String sha = commit.has("sha") ? commit.get("sha").asText() : "Unknown SHA";
                    String message = commit.at("/commit/message").asText("No message");
                    String author = commit.at("/commit/author/name").asText("Unknown author");
                    String date = commit.at("/commit/author/date").asText("Unknown date");

                    System.out.printf("Commit SHA: %s%nAuthor: %s%nDate: %s%nMessage: %s%n", sha, author, date, message);

                    JsonNode commitDetails = getCommitDetails(request.getOwner(), request.getRepo(), sha);
                    if (commitDetails != null && commitDetails.has("files")) {
                        System.out.println("Değişen Dosyalar:");
                        commitDetails.get("files").forEach(file -> {
                            String fileName = file.has("filename") ? file.get("filename").asText() : "Unknown file";
                            int additions = file.has("additions") ? file.get("additions").asInt() : 0;
                            int deletions = file.has("deletions") ? file.get("deletions").asInt() : 0;

                            System.out.printf("   - Dosya: %s (Eklendi: %d, Silindi: %d)%n", fileName, additions, deletions);
                        });
                    } else {
                        System.out.println("Dosya değişiklikleri bulunamadı.");
                    }

                    System.out.println("----------------------------------------");
                    commits.add(commit);
                });
            }
        } catch (Exception e) {
            System.out.println("HATA: API çağrısı sırasında bir sorun oluştu: " + e.getMessage());
        }
        return commits;
    }

    @Override
    public JsonNode getCommitDetails(String owner, String repo, String sha) {
        // GitHub API'si üzerinden belirli bir commit'in detaylarını alma mantığı
        return null; // Gerçek uygulamada, GitHub API çağrısını yaparak detayları döndürmelisiniz.
    }

    @Override
    public void formatCommitDetails(String owner, String repo, String sha, CommitResponse commitResponse) {
        JsonNode commitDetails = getCommitDetails(owner, repo, sha);

        if (commitDetails != null && commitDetails.has("files")) {
            commitResponse.setFiles(commitDetails.get("files"));
        }
    }
}
