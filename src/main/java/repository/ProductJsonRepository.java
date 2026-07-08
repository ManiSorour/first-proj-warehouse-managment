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

import static com.sun.tools.jdeprscan.DeprDB.loadFromFile;
import static files.FileExporter.GSON;

public class ProductJsonRepository implements ProductRepository{

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final String filePath;

    private final Map<Integer, Product> storage = new LinkedHashMap<>();

    public ProductJsonRepository(String filePath) {
        this.filePath = filePath;
        loadFromFile();

    }

    private void loadFromFile() {
        Path path = Path.of(filePath);
        if (!Files.exists(path)){
            return;

        }
        try (FileReader Reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<Product>>() {}.getType();
            List<Product> products = GSON.fromJson(Reader , listType);
            if (products != null) {
                for (Product p : products){
                    storage.put(p.getId() , p);
                }

            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("خطا در خواندن فایل JSON: " + e.getMessage());

        }

    }

    private void persist(){
        try(FileWriter writer = new FileWriter(filePath)){
            GSON.toJson(new ArrayList<>(storage.values()), writer);

        } catch (IOException e) {
            System.err.println("خطا در نوشتن فایل JSON: " + e.getMessage());
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
