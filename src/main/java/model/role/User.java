package model.role;

public abstract class User {

    private int id ;
    private String username;
    private String passwordHash;
    private Role role;


    public User(int id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }



    public abstract boolean canViewPrices();

    public abstract boolean canEditStock();

    public abstract boolean canViewReports();


    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }


    @Override
    public String toString() {
        return username + "(" + role + ")" ;
    }
}
