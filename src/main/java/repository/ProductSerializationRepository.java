package repository;

import model.product.Product;

import java.io.*;
import java.util.*;

import static com.sun.tools.jdeprscan.DeprDB.loadFromFile;

public class ProductSerializationRepository implements ProductRepository{

    private final String filePath;
    private final Map<Integer, Product> storage = new LinkedHashMap<>();

    public ProductSerializationRepository(String filePath) {
        this.filePath = filePath;
        loadFromFile();
    }
    @SuppressWarnings("unchecked")
    private void loadFromFile() {

        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){

            List<Product> products = (List<Product>) ois.readObject();
            for (Product p : products){
                storage.put(p.getId() , p);
            }



        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void persist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(new ArrayList<>(storage.values()));
        } catch (IOException e) {
            System.err.println("خطا در نوشتن فایل سریالایز‌شده: " + e.getMessage());
        }
    }



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
        persist();
    }

    @Override
    public void update(Product product) {
        storage.put(product.getId(), product);
        persist();
    }

    @Override
    public void delete(int id) {
        storage.remove(id);
        persist();
    }
}
