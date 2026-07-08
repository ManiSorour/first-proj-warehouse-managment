package repository;

import model.product.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> findAll();

    Optional<Product> findById( int id);

    void save(Product product);

    void update(Product product);


    void delete(int id);


}
