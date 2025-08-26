package committracker.model;

public class Repository {
    private String id;
    private String name;
    private String owner;
    private String url;

    public Repository() {}

    public Repository(String id, String name, String owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
