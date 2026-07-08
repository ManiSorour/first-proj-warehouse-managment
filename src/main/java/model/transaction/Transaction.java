package model.transaction;

import model.product.Product;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int id ;
    private Product product ;
    private TransactionType type ;
    private int quantity ;
    private LocalDateTime dateTime ;
    private String performedByUsername ;

    public Transaction(int id, Product product, TransactionType type,         //-----> برای اینکه اگر تراکننشی الان ثبت میشه لوکال دیت تایم . الان رو بزنیم تا ثبت شه اگر نه سراغ کانستراکتور بعدی بره <----
                       int quantity, String performedByUsername) {
        this(id, product, type, quantity, performedByUsername, LocalDateTime.now());
    }

    public Transaction(int id, Product product, TransactionType type,
                       int quantity, String performedByUsername, LocalDateTime dateTime) {
        this.id = id;
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.performedByUsername = performedByUsername;
        this.dateTime = dateTime;

    }

    private double getTotalAmount(){
        double unitPrice = (type == TransactionType.SELL)
                ? product.getSellPrice()
                : product.getPurchasePrice();
        return unitPrice * quantity ;
    }

    private String getFormattedDateTime(){
        return dateTime.format(FORMATTER);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getPerformedByUsername() {
        return performedByUsername;
    }

    public void setPerformedByUsername(String performedByUsername) {
        this.performedByUsername = performedByUsername;
    }

    @Override
    public String toString() {
        return "[" + getFormattedDateTime() + "] " + type + " - " +
                product.getName() + " x" + quantity +
                " = " + getTotalAmount() + " (توسط: " + performedByUsername + ")";
    }




}
