import files.FileExporter;
import files.ReportData;
import files.ReportService;
import model.product.Product;
import model.role.*;
import model.transaction.Transaction;
import repository.*;
import service.AlertService;
import service.WareHouseService;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static database.configuration.DataBaseConnection.connection;
import static model.role.Role.WAREHOUSE_KEEPER;

public class Main {
        private static final Scanner scanner = new Scanner(System.in);
    private static WareHouseService wareHouseService;
    private static int nextProductId = 1;
    private static AlertService alertService;
    private static User currentUser;
    private static FileExporter fileExporter;



    public static void main(String[] args) {
        System.out.println("=== سیستم مدیریت انبار ===\n");

        ProductRepository repository = ChooseRepository();
         wareHouseService = new WareHouseService(repository);

        if (wareHouseService.getAllProducts().isEmpty()){
            initialProductSeed();
        }else {
            nextProductId =  wareHouseService.getAllProducts().stream().mapToInt(Product::getId).max().orElse(0) +1 ;
            System.out.println("✅ " + wareHouseService.getAllProducts().size() + " کالای قبلی از حافظه پایدار بارگذاری شد.\n");


            alertService.startBackgroundMonitoring(wareHouseService, Long.valueOf(15000));

        if (!login()){
                System.out.println("ورود ناموفق. برنامه بسته می‌شود.");
                return;
            }

            boolean running = true;
            while (running){

                printMenu();
                String choice = scanner.nextLine().trim();
                switch (choice){
                    case "1" -> viewAllProducts();
                    case "2" -> addProduct();
                    case "3" -> sellProduct();
                    case "4" -> purchaseProduct();
                    case "5" -> viewLowStock();
                    case "6" -> viewTransactionHistory();
                    case "7" -> generateAndExportReport();
                    case "0" -> {running = false;
                        alertService.stopBackgroundMonitoring();
                        System.out.println("خروج از برنامه. خدانگهدار!");
                    }
                    default -> System.out.println("گزینه نامعتبر است.\n");
                }
            }
        }
    }
    private static void printMenu(){
        System.out.println("---------------------------------");
        System.out.println("1) مشاهده لیست کالاها");
        System.out.println("2) ثبت کالای جدید");
        System.out.println("3) ثبت فروش کالا");
        System.out.println("4) ثبت خرید (افزایش موجودی)");
        System.out.println("5) مشاهده هشدار موجودی کم");
        System.out.println("6) مشاهده تاریخچه تراکنش‌ها");
        System.out.println("7) گزارش نهایی (ارزش انبار + پرفروش‌ها)");
        System.out.println("0) خروج");
        System.out.print("انتخاب شما: ");
    }
    private static ProductRepository ChooseRepository(){

        System.out.println("داده‌های کالا کجا ذخیره شوند؟");
        System.out.println("1) فقط در حافظه (In-Memory) - با بستن برنامه پاک می‌شود");
        System.out.println("2) فایل JSON (products.json) - پایدار و خواناست");
        System.out.println("3) Java Serialization (products.dat) - پایدار و باینری است");
        System.out.print("انتخاب شما: ");
        String choice = scanner.nextLine().trim();
       return switch (choice) {

           case "2" -> {
               System.out.println("→ در حال استفاده از ProductJsonRepository (products.json)\n");
               yield new ProductJsonRepository("products.json");
           }
           case "3" -> {
               System.out.println("→ در حال استفاده از ProductSerializationRepository (products.dat)\n");
               yield new ProductSerializationRepository("products.dat");
           }


           default -> {
               System.out.println("→ در حال استفاده از ProductInMemoryRepository (بدون پایداری)\n");
               yield new ProductInMemoryRepository();


           }

       };
    }
    private static boolean login(){

        System.out.println("انتخاب نقش برای ورود:");
        System.out.println("1) مدیر (Admin)");
        System.out.println("2) انباردار (Warehouse Keeper)");
        System.out.println("3) بازرس (Inspector)");
        System.out.print("انتخاب شما: ");

        String choice = scanner.nextLine().trim();
        currentUser = switch (choice){

            case "1" -> new Admin(1, "ali_admin", "hashed_pw");
            case "2" -> new InventoryManager(2, "ali_keeper", "hashed_pw");
            case "3" -> new Inspector(3 , "alialialiinspector" , "123456");
            default -> null ;
        };

        if (currentUser != null) {
            System.out.println("\nخوش آمدی " + currentUser + "\n");
            return true;
        }
        return false;
    }

    private static void initialProductSeed(){

        wareHouseService.addProduct( new Product(nextProductId++, "لپ‌تاپ ایسوس", "LP-001", "الکترونیک", 25000000, 29000000, 8, 3),
                new Admin(0, "system", "system"));
        wareHouseService.addProduct(
                new Product(nextProductId++, "ماوس لاجیتک", "MS-002", "لوازم جانبی", 350000, 450000, 2, 5),
                new Admin(0, "system", "system"));

        wareHouseService.addProduct(
                new Product(nextProductId++, "کیبورد مکانیکال", "KB-003", "لوازم جانبی", 900000, 1200000, 15, 4),
                new Admin(0, "system", "system"));

    }
    private static void viewAllProducts(){
         List<Product> products = wareHouseService.getAllProducts();
        System.out.println("\n--- لیست کالاها ---");
        for(Product p : products){
            StringBuilder line = new StringBuilder();
            line.append(p.getId()).append(". ").append(p.getName())
                    .append(" [").append(p.getCode()).append("] - موجودی: ").append(p.getQuantity())
                    .append(" - وضعیت: ").append(p.getStatus());
            if (currentUser.canEditStock()){
                line.append(" - قیمت فروش: ").append(p.getSellPrice());

            }
            System.out.println(line);
        }
        System.out.println();
    }
    private static void addProduct(){
        if (!currentUser.canEditStock()) {
            System.out.println("\n شما اجازه ثبت کالا را ندارید.\n");
            return;
        }
        System.out.print("نام کالا: ");
        String name = scanner.nextLine();
        System.out.print("کد کالا: ");
        String code = scanner.nextLine();
        System.out.print("دسته‌بندی: ");
        String category = scanner.nextLine();
        System.out.print("قیمت خرید: ");
        double purchasePrice = Double.parseDouble(scanner.nextLine());
        System.out.print("قیمت فروش: ");
        double sellPrice = Double.parseDouble(scanner.nextLine());
        System.out.print("تعداد موجودی اولیه: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.print("حداقل موجودی مجاز (برای هشدار): ");
        int minStock = Integer.parseInt(scanner.nextLine());
        Product product = new Product(nextProductId++, name, code, category, purchasePrice, sellPrice, quantity, minStock);
        wareHouseService.addProduct(product, currentUser);
        System.out.println("✅ کالا با موفقیت ثبت شد.\n");

    }
    private static void sellProduct(){
        System.out.print("شناسه (id) کالا برای فروش: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("تعداد فروش: ");
        int qty = Integer.parseInt(scanner.nextLine());
        try {
            wareHouseService.sellProduct(id , qty , currentUser);

        }catch (Exception e) {
            System.out.println("خطا: " + e.getMessage() + "\n");
        }
    }
    private static void purchaseProduct(){
        if (!currentUser.canEditStock()){
            System.out.println("\n شما اجازه ثبت خرید ندارید.\n");
            return;
        }
        System.out.print("شناسه (id) کالا برای افزایش موجودی: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("تعداد خرید: ");
        int qty = Integer.parseInt(scanner.nextLine());
        try{
            wareHouseService.purchaseProduct(id , qty , currentUser);
            System.out.println(" خرید با موفقیت ثبت شد.\n");

        }catch (Exception e){
            System.out.println("خطا: " + e.getMessage() + "\n");

        }
    }private static void viewLowStock(){

        List<Product> lowStock = AlertService.checkLowStock(wareHouseService.getAllProducts());
        System.out.println("\n--- کالاهای با موجودی کم ---");
        if (lowStock.isEmpty()){
            System.out.println("everything just fine ! ...");

        }else {
            lowStock.forEach(p -> System.out.println("⚠ " + p.getName() + " - باقی‌مانده: " + p.getQuantity()));
        }
        System.out.println();
    }
    private static void viewTransactionHistory(){
        if (!currentUser.canViewReports() && currentUser.getRole() != Role.WAREHOUSE_KEEPER) {
            System.out.println("\n شما اجازه مشاهده تاریخچه را ندارید.\n");
            return;
        }
        List<Transaction> history = wareHouseService.getTransactionHistory();
        System.out.println("\n--- تاریخچه تراکنش‌ها ---");
        if (history.isEmpty()){
            System.out.println("(هنوز هیچ تراکنشی در دیتابیس ثبت نشده - این بخش با اتصال واقعی JDBC کار می‌کند)");
        }else {
            history.forEach(System.out :: println);
        }
        System.out.println();
    }
    private static void generateAndExportReport(){
        if (!currentUser.canViewReports()){
            System.out.println("\n شما اجازه مشاهده گزارش‌ها را ندارید.\n");
            return;
        }
            List<Product> products = wareHouseService.getAllProducts();
            List<Transaction> transactions = wareHouseService.getTransactionHistory();
        ReportData reportData = ReportService.generateReport(products,transactions);

        System.out.println("\n--- گزارش نهایی ---");
        System.out.println(reportData);

        System.out.print("\nخروجی را ذخیره کنم؟ (json/txt/no): ");
        String format = scanner.nextLine().trim().toLowerCase();
        try {
            switch (format) {
                case "json" -> {
                    fileExporter.exportToJson("report.json", reportData);
                    System.out.println("✅ ذخیره شد در report.json");
                }

                case "txt" -> {
                    fileExporter.exportToTextFile("report.txt", reportData);
                    System.out.println("✅ ذخیره شد در report.txt");
                }
                default -> System.out.println("خروجی ذخیره نشد.");
            }
        } catch (IOException e) {
            System.out.println("خطا در ذخیره فایل: " + e.getMessage());
        }


    }








}
