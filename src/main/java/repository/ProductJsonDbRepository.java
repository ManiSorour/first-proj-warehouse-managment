package repository;

import database.configuration.GenericRepository;
import database.connectToDb.ProductGenericRepository;
import model.product.Product;

import java.util.List;
import java.util.Optional;

public class ProductJsonDbRepository implements ProductRepository {
    private ProductRepository jsonRepository;
    private final ProductGenericRepository dbRepository = new ProductGenericRepository();

    public ProductJsonDbRepository(ProductRepository jsonRepository) {
        this.jsonRepository = jsonRepository;
    }

    @Override
    public List<Product> findAll() {

        return jsonRepository.findAll();
    }

    @Override
    public Optional<Product> findById(int id) {
        return jsonRepository.findById(id);
    }

    @Override
    public void save(Product product) {
        jsonRepository.save(product);
        Product existingInDb = dbRepository.findByCode(product.getCode());
        if (existingInDb == null) {
            dbRepository.save(product);
            System.out.println("→ کالای «" + product.getName() + "» در دیتابیس هم برای همیشه ثبت شد.");
        } else {
            System.out.println("→ کالایی با کد «" + product.getCode() + "» از قبل در دیتابیس هست، از ثبت دوباره صرف‌نظر شد.");
        }
    }

    @Override
    public void update(Product product) {
        dbRepository.update(product);

        Product existingInDb = dbRepository.findByCode(product.getCode());
        if (existingInDb == null) {

            Product ProductWithRealId = new Product(
                    existingInDb.getId(),
                    product.getName(),
                    product.getCode(),
                    product.getCategory(),
                    product.getPurchasePrice(),
                    product.getSellPrice(),
                    product.getQuantity(),
                    product.getMinStockLevel()
            );
            dbRepository.update(ProductWithRealId);
        } else {
            dbRepository.save(product);
        }
    }

    @Override
    public void delete(int id) {
        Optional<Product> product = jsonRepository.findById(id);

        jsonRepository.delete(id);

        if (product.isPresent()) {
            Product p = product.get();
            Product existingInDb = dbRepository.findByCode(p.getCode());

            if (existingInDb != null) {
                dbRepository.delete(existingInDb.getId());


            }
        }

    }
}
