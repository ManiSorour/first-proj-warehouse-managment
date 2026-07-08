package model.role;

public class Inspector extends User {

    // فقط اجازه داره که قیمت و موجودی و گزارش هارو (مشاهده) کنه


    public Inspector(int id, String username, String passwordHash ) {
        super(id, username, passwordHash, Role.INSPECTOR);
    }

    @Override
    public boolean canViewPrices() {
        return false;
    }

    @Override
    public boolean canEditStock() {
        return false;
    }

    @Override
    public boolean canViewReports() {
        return true;
    }
}
