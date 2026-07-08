package repository;

import model.product.Product;

import java.util.*;

public class ProductInMemoryRepository implements ProductRepository{

    private final  Map<Integer , Product> storage  = new HashMap<>();


    @Override
    public List<Product> findAll() {
        return new ArrayList<>(storage.values());

    }

    @Override
    public Optional<Product> findById(int id) {
        return Optional.ofNullable(storage.get(id));

    }

    @Override
    public void save(Product product) {
        storage.put(product.getId(), product);

    }

    @Override
    public void update(Product product) {
        storage.put(product.getId(), product);

    }

    @Override
    public void delete(int id) {
        storage.remove(id);

    }
}
