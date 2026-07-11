package repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.product.Product;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static files.FileExporter.GSON;

public class ProductJsonRepository implements ProductRepository{

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Integer, Product> storage = new HashMap<>();
    private String filePath;

    public ProductJsonRepository(String filePath) {
        this.filePath = filePath;
        loadFromFile();
    }

    private void loadFromFile(){

        File file = new File(filePath);
        if (!file.exists()){
            return;
        }
        FileReader reader = null;


        try {
            reader = new FileReader(file);
            Type listType  =new TypeToken<ArrayList<Product>>() {}.getType();
            List<Product> products = GSON.fromJson(reader , listType);
            if (products != null){
                for (Product product : products){
                    storage.put(product.getId() , product);
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("خطا در خواندن فایل: " + e.getMessage());
        }
            finally {
            if (reader != null){
                try {
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }


    private void change(){
        File file = new File(filePath);

        try(FileWriter writer = new FileWriter(filePath)) {
            GSON.toJson(new ArrayList<>(storage.values()) , writer);
        } catch (IOException e) {
            System.err.println("خطا در نوشتن فایل: " + e.getMessage());
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
