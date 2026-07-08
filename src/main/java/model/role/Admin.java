package model.role;

public class Admin extends User{


    public Admin(int id, String username, String passwordHash) {
        super(id, username, passwordHash, Role.ADMIN);
    }

    @Override
    public boolean canViewPrices() {
        return true;
    }

    @Override
    public boolean canEditStock() {
        return true;
    }

    @Override
    public boolean canViewReports() {
        return true;
    }
}
