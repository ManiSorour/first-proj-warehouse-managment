package repository;

import model.product.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ProductXmlRepository implements ProductRepository{

    private final String filePath ;

    HashMap<Integer , Product> storage = new HashMap<>() ;
















    @Override
    public List<Product> findAll() {
        return null;
    }

    @Override
    public Optional<Product> findById(int id) {
        return Optional.empty();
    }

    @Override
    public void save(Product product) {

    }

    @Override
    public void update(Product product) {

    }

    @Override
    public void delete(int id) {

    }
}
