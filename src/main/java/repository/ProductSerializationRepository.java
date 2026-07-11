package repository;

import model.product.Product;

import java.io.*;
import java.util.*;

public class ProductSerializationRepository implements ProductRepository{

    private final String filePath;
    private final Map<Integer, Product> storage = new HashMap<>();

    public ProductSerializationRepository(String filePath) {
        this.filePath = filePath;
        loadFromFile();
    }

    private void loadFromFile() {


        File file = new File(filePath);
         if ( !file.exists()){
             return;
         }
        try {

            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            List<Product> products  = (List<Product>) objectInputStream.readObject();
            for (Product p : products){
               storage.put(p.getId() , p);
            }
        } catch (IOException e) {
             System.err.println("خطا در خواندن فایل سریالایز‌شده: " + e.getMessage());

         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }



    private void change(){

        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream( new FileOutputStream(filePath));
            objectOutputStream.writeObject(new ArrayList<>(storage.values()));


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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
        change();
    }

    @Override
    public void update(Product product) {
        storage.put(product.getId(), product);
       change();
    }

    @Override
    public void delete(int id) {
        storage.remove(id);
      change();
    }
}
