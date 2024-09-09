package io.oobeya.committracker.dto;

public class CommitsRequest {
    private String owner;
    private String repo;

    // TODO : lombok kullanabilrişiz, requestlerde settera ihityaç yok
    public CommitsRequest(String owner, String repo) {
        this.owner = owner;
        this.repo = repo;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }
}