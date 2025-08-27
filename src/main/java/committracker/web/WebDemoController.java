package committracker.web;

import committracker.dto.CommitsRequest;
import committracker.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Web Demo Controller - Portfolio için web arayüzü
 */
@Controller
public class WebDemoController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "CommitTracker Web Demo");
        return "index";
    }

    @PostMapping("/api/commits")
    @ResponseBody
    public List<JsonNode> getCommits(@RequestBody CommitsRequest request) {
        try {
            VCSService service = createService(request.getPlatform(), request.getAccessToken());
            return service.getCommits(request);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching commits: " + e.getMessage());
        }
    }

    @GetMapping("/demo")
    public String demo() {
        return "demo";
    }

    private VCSService createService(String platform, String accessToken) {
        return switch (platform.toLowerCase()) {
            case "github" -> new GitHubService(accessToken);
            case "gitlab" -> new GitLabService(accessToken);
            case "bitbucket" -> new BitbucketService(accessToken);
            case "azuredevops" -> new AzureDevOpsService(accessToken);
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleError(Exception e) {
        return "{\"error\":\"" + e.getMessage() + "\"}";
    }
}
