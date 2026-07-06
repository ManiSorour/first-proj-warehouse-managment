package model.role;

public class InventoryManager extends User{

    //فقط اجازه ثبت و ویرایش موجودی کالا


    public InventoryManager(int id, String username, String passwordHash ) {
        super(id, username, passwordHash, Role.WAREHOUSE_KEEPER);
    }

    @Override
    public boolean canViewPrices() {
        return false;
    }

    @Override
    public boolean canEditStock() {
        return true;
    }

    @Override
    public boolean canViewReports() {
        return false;
    }






}
