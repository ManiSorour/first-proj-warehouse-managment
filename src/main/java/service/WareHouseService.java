package service;

<<<<<<< HEAD
import database.connectToDb.ProductGenericRepository;
import database.connectToDb.TransactionGenericRepository;
=======
import database.connectToDb.ProductDao;
import database.connectToDb.TransactionDao;
>>>>>>> 8305ad8bda157d2e01bdfdc9b08401173f01d59b
import model.product.Product;
import model.role.User;
import model.transaction.Transaction;
import model.transaction.TransactionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WareHouseService {

    private final Map<Integer, Product> inventory = new HashMap<>();

    private final ProductGenericRepository productDao = new ProductGenericRepository();
    private final TransactionGenericRepository transactionDao = new TransactionGenericRepository();

    private final ProductDao productDao = new ProductDao();
    private final TransactionDao transactionDao = new TransactionDao();


    public void loadInventoryFromDatabase() {
        List<Product> Products = productDao.findAll();
        for (Product p : Products) {
            inventory.put(p.getId(), p);
        }
    }

    public void addProduct(Product product, User performedBy) {                      // بررسی سطح دسترسی و ذخیره سازی دو مرحله ای هم در دیتا بیس هم در خاقظه
        if (!performedBy.canEditStock()) {
            throw new SecurityException("this user can't add product");
        }
        productDao.save(product);
        inventory.put(product.getId(), product);

    }

    public Optional<Product> findProductByCode (String code) {
        return inventory.values().stream()
                .filter(p -> p.getCode().equalsIgnoreCase(code))
                .findFirst();
    }

    public List<Product> getAllProducts() {
        return List.copyOf(inventory.values());
    }

    public void sellProduct(int productId, int quantity, User performedBy) {

        Product product = inventory.get(productId);

        if (product == null) {
            throw new IllegalArgumentException("product with this id not found");
        }
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("there is no product right now");
        }

        product.setQuantity(product.getQuantity() - quantity);//کاهش موجودی در حافظه


        productDao.update(product);

        Transaction transaction = new Transaction(
                0, product, TransactionType.SELL, quantity, performedBy.getUsername());

        transactionDao.save(transaction);


    }


    public void purchaseProduct(int productId, int quantity, User performedBy) {
        if (!performedBy.canEditStock()) {

            throw new SecurityException("این کاربر اجازه ثبت موجودی را ندارد");
        }
        Product product = inventory.get(productId);

        if (product == null) {
            throw new IllegalArgumentException("کالایی با این شناسه پیدا نشد");

        }

        product.setQuantity(product.getQuantity() + quantity);
        productDao.update(product);


        Transaction transaction = new Transaction(
                0, product, TransactionType.PURCHASE, quantity, performedBy.getUsername());
        transactionDao.save(transaction);
    }


    public List<Transaction> getTransactionHistory() {
        return transactionDao.findAll();


    }
}
