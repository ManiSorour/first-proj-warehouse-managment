package repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.transaction.Transaction;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionJsonRepository implements TransactionRepository {


    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DATE_FORMAT)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                    (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), DATE_FORMAT))
            .create();

    private final List<Transaction> storage = new ArrayList<>();

    private String filePath;

    public TransactionJsonRepository(String filePath) {
        this.filePath = filePath;
        loadFromFile();
    }

    private void loadFromFile() {

        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            Type listType = new TypeToken<ArrayList<Transaction>>() {
            }.getType();
            List<Transaction> transactions = GSON.fromJson(reader, listType);
            if (transactions != null) {
                storage.addAll(transactions);
            }

        } catch (FileNotFoundException e) {
            System.err.println("خطا در خواندن فایل: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void change() {
        Path mainPath = Path.of(filePath);
        Path temporalPath = Path.of(filePath + ".tmp");

        try (FileWriter writer = new FileWriter(temporalPath.toFile());){        // اگر توی ریسورس نذارمش فایل .tmp  بسته نمیشه هیچوقت
            GSON.toJson(storage, writer);
        } catch (IOException e) {
            System.err.println("خطا در نوشتن فایل: " + e.getMessage());
            return;
        }
        try {
            Files.move(temporalPath, mainPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("خطا در جایگزینی اتمیک فایل: " + e.getMessage());
        }


    }


        @Override
        public List<Transaction> findAll () {
            return new ArrayList<>(storage);
        }



        @Override
        public void save (Transaction transaction){
            storage.add(transaction);
            change();
        }


    }
