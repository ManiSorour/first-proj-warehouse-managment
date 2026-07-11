package service;

import model.product.Product;

import java.util.List;
import java.util.stream.Collectors;

public class AlertService {

    private volatile boolean monitoring  = false;
    private Thread monitorThread ;


    public static List<Product> checkLowStock(List<Product> products) {
        return products.stream()
                .filter(Product::isLowStock)
                .collect(Collectors.toList());
    }


    public void startBackgroundMonitoring(WareHouseService wareHouseService , Long intervalMillis){

        monitoring = true;
        monitorThread = new Thread(() -> {

            while (monitoring){

                List<Product> lowStockItems = checkLowStock(wareHouseService.getAllProducts());
                if (!lowStockItems.isEmpty()) {
                    System.out.println("\n⚠ هشدار موجودی کم برای " + lowStockItems.size() + " کالا:");

                    lowStockItems.forEach(p -> System.out.println("   - " + p.getName() + " (باقی‌مانده: " + p.getQuantity() + ")"));
                }
                try {
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    monitorThread.setDaemon(true);
    monitorThread.start();
    }


    public void stopBackgroundMonitoring(){
        monitoring = false;
        if (monitorThread != null){
            monitorThread.interrupt();
        }


    }


}
