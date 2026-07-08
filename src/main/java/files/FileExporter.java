package files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class FileExporter {

<<<<<<< HEAD
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
=======
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
>>>>>>> 8305ad8bda157d2e01bdfdc9b08401173f01d59b
//for user read
        public void exportToTextFile(String path, ReportData data)throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))){

                writer.write(data.toString());

            }


        }
//serialize
    public void exportToJson(String path, ReportData data)throws IOException {
        try (Writer writer = new FileWriter(path)){
            GSON.toJson(data);
        }

    }
//deserilize
    public ReportData importFromJson(String path) throws IOException {
        try (Reader reader = new FileReader(path)) {
            return GSON.fromJson(reader, ReportData.class);
        }
    }

    public void exportToBinary(String path, ReportData data) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(data);
        }
    }

    public ReportData importFromBinary(String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return (ReportData) ois.readObject();
        }
    }





}
