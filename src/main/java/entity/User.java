package entity;

public class User {
    private String username;
    private String userRole;
    private long userId;
    
    public User() {
    }

    public User(String username, String userRole, long userId) {
        this.username = username;
        this.userRole = userRole;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    
    public long getUserId() {
        return userId;
    }
    
    public void setUserId(long userId) {
        this.userId = userId;
    }
}
