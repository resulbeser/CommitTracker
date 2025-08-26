package committracker.util;

public class GitLabApiException extends Exception {
    public GitLabApiException(String message) {
        super(message);
    }

    public GitLabApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
