package committracker.dto;

public class CommitFileResponse {
    private String fileName;
    private int additions;
    private int deletions;

    public CommitFileResponse(String fileName, int additions, int deletions) {
        this.fileName = fileName;
        this.additions = additions;
        this.deletions = deletions;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getAdditions() {
        return additions;
    }

    public void setAdditions(int additions) {
        this.additions = additions;
    }

    public int getDeletions() {
        return deletions;
    }

    public void setDeletions(int deletions) {
        this.deletions = deletions;
    }
}
