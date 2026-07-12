package model.product;

import java.util.Objects;

public class Product {
    private int id ;
    private String name;
    private String code;        //barcode
    private String category;
    private double purchasePrice ;
     private double sellPrice;
    private int quantity;
    private int  minStockLevel;
    private ProductStatus status;


    public Product(int id, String name, String code, String category, double purchasePrice, double sellPrice, int quantity, int minStockLevel, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.sellPrice = sellPrice;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        updateStatus();
    }

    public Product(int id, String name, String code, String category, double purchasePrice, double sellPrice, int quantity, int minStockLevel) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.sellPrice = sellPrice;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        updateStatus();
    }

    private void updateStatus() {

        this.status = (quantity <= 0)
                ? ProductStatus.OUT_OF_STOCK
                : ProductStatus.AVAILABLE;
    }

    public boolean isLowStock(){
        return quantity>0 && quantity <= minStockLevel ;
    }

    public double totalValue(){
        return quantity * purchasePrice ;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public ProductStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', code='" + code +
                "', quantity=" + quantity + ", status=" + status + "}";
    }

    public double getTotalValue() {
        return quantity * purchasePrice;
    }
}
