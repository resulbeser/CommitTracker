package committracker.service;

import committracker.dto.CommitResponse;
import committracker.dto.CommitsRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BitbucketService implements VCSService {

    private final String accessToken;

    public BitbucketService(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public List<JsonNode> getCommits(CommitsRequest request) {
        // Bitbucket API'si üzerinden commit'leri alma mantığı
        // Örnek: Bitbucket API çağrısı yaparak sonuçları döndürme
        return null; // Gerçek uygulamada, Bitbucket API çağrısını yaparak bu listeyi döndürmelisiniz.
    }

    @Override
    public JsonNode getCommitDetails(String owner, String repo, String sha) {
        // Bitbucket API'si üzerinden belirli bir commit'in detaylarını alma mantığı
        return null; // Gerçek uygulamada, Bitbucket API çağrısını yaparak detayları döndürmelisiniz.
    }

    @Override
    public void formatCommitDetails(String owner, String repo, String sha, CommitResponse commitResponse) {
        // Bitbucket'a özgü commit detaylarını ekleme
        JsonNode commitDetails = getCommitDetails(owner, repo, sha);

        if (commitDetails != null && commitDetails.has("files")) {
            commitResponse.setFiles(commitDetails.get("files"));
        }
    }
}
