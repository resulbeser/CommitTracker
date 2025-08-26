package committracker.model;

public class User {
    private String id;
    private String name;
    private String username;
    private String displayName;
    private String email;

    public User() {}

    public User(String id, String name, String username) {
        this.id = id;
        this.name = name;
        this.username = username;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
