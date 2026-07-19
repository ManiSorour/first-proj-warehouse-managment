package service;


import database.configuration.DataBaseConnection;
import database.connectToDb.ProductGenericRepository;
import database.connectToDb.TransactionGenericRepository;

import model.product.Product;
import model.product.ProductStatus;
import model.role.User;
import model.transaction.Transaction;
import model.transaction.TransactionType;
import repository.ProductRepository;
import repository.TransactionJsonRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class WareHouseService {

    private final Map<Integer, Product> inventory = new HashMap<>();
    private final ProductRepository productRepository;
    private final ProductGenericRepository productDao = new ProductGenericRepository();
    private final TransactionGenericRepository transactionDao = new TransactionGenericRepository();

    private final  TransactionJsonRepository transactionJsonRepository = new TransactionJsonRepository("transactions.json");


    public WareHouseService(ProductRepository repository) {
        this.productRepository = repository;
    }

    public void addProduct(Product product, User performedBy) {
        if (!performedBy.canEditStock()) {
            throw new SecurityException("this user can't add product");
        }
        productRepository.save(product);
    }

    public Optional<Product> findProductByCode(String code) {
        return productRepository.findAll().stream()
                .filter(p -> p.getCode().equalsIgnoreCase(code))
                .findFirst();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void sellProduct(int productId, int quantity, User performedBy) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("product with this id not found"));

        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("there is no product right now");
        }

        product.setQuantity(product.getQuantity() - quantity);

        Transaction transaction = new Transaction(0, product, TransactionType.SELL, quantity, performedBy.getUsername());


        Product dbProduct = productDao.findByCode(product.getCode());

        if (dbProduct != null) {
            Connection conn = null;
            try {
                conn = DataBaseConnection.getConnection();
                conn.setAutoCommit(false);

                Product vasl = new Product(
                        dbProduct.getId(), product.getName(), product.getCode(), product.getCategory(),
                        product.getPurchasePrice(), product.getSellPrice(),
                        product.getQuantity(), product.getMinStockLevel()
                );
                productDao.update(conn, vasl);
                transactionDao.save(conn, transaction);
                conn.commit();
            } catch (SQLException e) {
                System.err.println("خطا در ثبت اتمیک فروش، Rollback انجام شد: " + e.getMessage());
                try {
                    if (conn != null) conn.rollback();
                } catch (SQLException e1) {
                    System.err.println("خطا در Rollback: " + e1.getMessage());
                }
            }finally {
                try {
                    if(conn != null)
                        conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("خطا در بازگرداندن autoCommit: " + e.getMessage());
                }
            }
        }


        transactionDao.save(transaction);
    }


    public void purchaseProduct(int productId, int quantity, User performedBy) {
        if (!performedBy.canEditStock()) {
            throw new SecurityException("این کاربر اجازه ثبت موجودی را ندارد");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("کالایی با این شناسه پیدا نشد"));

        product.setQuantity(product.getQuantity() + quantity);
        productRepository.update(product);
/// program ends here

        Transaction transaction = new Transaction(0, product, TransactionType.PURCHASE, quantity, performedBy.getUsername());
        transactionDao.save(transaction);
    }


    //---- edit product
    public void updateProduct(int productId, String name, String code, String category, double purchasePrice, double sellPrice, int minStockLevel, User performedBy) {

        if (!performedBy.canEditStock()) {
            throw new SecurityException("این کاربر اجازه ویرایش کالا را ندارد");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("کالایی با این شناسه پیدا نشد "));      //name is handling a empty optional
        product.setName(name);
        product.setCode(code);
        product.setCategory(category);
        product.setPurchasePrice(purchasePrice);
        product.setSellPrice(sellPrice);
        product.setMinStockLevel(minStockLevel);


        productRepository.update(product);
    }

    public void deleteProduct(int productID, User performedBy) {
        if (!performedBy.canEditStock()) {
            throw new SecurityException("این کاربر اجازه حذف را ندارد");
        }
        productRepository.findById(productID)
                .orElseThrow(() -> new IllegalArgumentException("کالایی با این شناسه پیدا نشد"));
        productRepository.delete(productID);
    }

    //---- filter
    public List<Product> findProductsByPriceRange(double minPrice, double maxPrice) {

        if (maxPrice > minPrice) {
            System.out.println("حداثر قیمت نمیتواند از حداقل قیمت بیشتر باشد ");
        }

        return productRepository.findAll().stream()
                .filter(p -> p.getSellPrice() <= maxPrice && p.getSellPrice() >= minPrice)
                .collect(Collectors.toList());

    }

    public List<Product> findProductsByStatus(ProductStatus status) {
        return productRepository.findAll().stream()
                .filter(p -> p.getStatus() == status)
                .collect(Collectors.toList());
    }


    public List<Product> findProductsByCategory(String category) {
        return productRepository.findAll().stream().
                filter(p -> p.getCategory().equalsIgnoreCase(category)).
                collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return productRepository.findAll().stream().map(Product::getCategory).
                distinct().
                collect(Collectors.toList());
    }


    public List<Transaction> getTransactionHistory() {
        return transactionJsonRepository.findAll();
    }




}
