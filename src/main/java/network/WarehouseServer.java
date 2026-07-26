package network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.product.Product;
import service.WareHouseService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class WarehouseServer {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private WareHouseService wareHouseService;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public WarehouseServer(WareHouseService wareHouseService) {
        this.wareHouseService = wareHouseService;

    }

    public void startServer(int port) {
        if (running) {
            System.out.println("سرور از قبل فعال است");
            return;
        }
        running = true;


        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);


                System.out.println("Socket سرور مانیتورینگ روی پورت " + port + " فعال شد.");

                while (running) {
                    Socket clientSocket = serverSocket.accept(); // منتظر اتصال بعدی می‌مونه

                    Thread clientThread = new Thread(() -> {
                        try {
                            respondToClient(clientSocket);
                        } catch (IOException e) {
                            System.out.println(e.getMessage() + "خطا در اتصال کاربر ");
                        }
                    });
                    clientThread.setDaemon(true);
                    clientThread.start();
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("[Socket] خطای سرور: " + e.getMessage());
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }


    private void respondToClient(Socket clientSocket) throws IOException {


        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));) {

            String clientCommand = in.readLine();
            if (clientCommand == null) {
                return;
            }
            switch (clientCommand.trim().toUpperCase()) {
                case "LIST" -> out.println(gson.toJson(wareHouseService.getAllProducts()));

                case "LOWSTOCK" -> {
                    List<Product> lowStock = wareHouseService.getAllProducts().stream()
                            .filter(Product::isLowStock)
                            .collect(Collectors.toList());
                    out.println(gson.toJson(lowStock));
                }
                default -> out.println("فقط از list یا  low stock  میتوانید استفاده کنید");
            }
        } catch (IOException e) {
            System.err.println("Socket خطا در ارتباط با کلاینت: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }


    }


    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
            }
            System.out.println(" سرور متوقف شد");

        } catch (IOException e) {
            System.err.println("Socket خطا در بستن سرور: " + e.getMessage());

        }
    }
}
