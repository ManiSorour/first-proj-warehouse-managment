import annotation.MenuCommand;
import files.FileExporter;
import files.ReportData;
import files.ReportService;
import model.product.Product;
import model.product.ProductStatus;
import model.role.*;
import model.transaction.Transaction;
import repository.*;
import service.AlertService;
import service.AuthService;
import service.UserService;
import service.WareHouseService;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static model.role.Role.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static WareHouseService wareHouseService;
    private static int nextProductId = 1;
    private static AlertService alertService = new AlertService();
    private static User currentUser;
    private static FileExporter fileExporter = new FileExporter();

    private static final UserService userService = new UserService();


    public static void main(String[] args) {
        System.out.println("=== سیستم مدیریت انبار ===\n");

        ProductRepository repository = ChooseRepository();
        wareHouseService = new WareHouseService(repository);


        if (wareHouseService.getAllProducts().isEmpty()) {
            initialProductSeed();
        } else {
            nextProductId = wareHouseService.getAllProducts().stream().mapToInt(Product::getId).max().orElse(0) + 1;
            System.out.println("✅ " + wareHouseService.getAllProducts().size() + " کالای قبلی از حافظه پایدار بارگذاری شد.\n");
        }

        alertService.startBackgroundMonitoring(wareHouseService, Long.valueOf(180_000));

        currentUser = login();
        if (currentUser == null) {
            System.out.println("ورود ناموفق. برنامه بسته می‌شود.");
            return;
        }
        showMainMenu();

    }

    public static void showMainMenu() {

        List<Method> commands = Arrays.stream(Main.class.getDeclaredMethods())
                .filter(meth -> meth.isAnnotationPresent(MenuCommand.class))
                .filter(meth -> Main.isAllowedUser(meth))
                .collect(Collectors.toList());

        boolean running = true;

        while (running) {
            System.out.println("---------------------------------");

            for (int i = 0; i < commands.size(); i++) {
                MenuCommand cmd = commands.get(i).getAnnotation(MenuCommand.class);
                System.out.println((i + 1) + ") " + cmd.name());
            }
            System.out.println("0) خروج");
            System.out.print("انتخاب شما: ");

            String input = scanner.nextLine().trim();

            if (input.equals(0)) {
                running = false;
                alertService.stopBackgroundMonitoring();
                System.out.println("خروج از برنامه. خدانگهدار!");
            }


            try {
                Method selected = commands.get(Integer.parseInt(input) - 1);
                selected.setAccessible(true);

                System.out.println("LOG شروع اجرای: " + selected.getName());
                long start = System.currentTimeMillis();
                selected.invoke(null);
                long timer = System.currentTimeMillis() - start ;
                System.out.println("[LOG] پایان اجرای: " + selected.getName() + " (" + timer + " ms)");



            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        }

    }

    private static boolean isAllowedUser(Method meth) {

        MenuCommand commd = meth.getAnnotation(MenuCommand.class);
        Role[] userCapability = commd.selectedRole();
        if (userCapability.length == 0) {
            return true;
        }
        for (Role r : userCapability) {
            if (r == currentUser.getRole()) {
                return true;
            }
        }
        return false;

    }


    //----------------------------------------------------------------panel admin ---------
    private static void adminPanel() {

        if (currentUser.getRole() != ADMIN) {
            System.out.println("\n⚠ فقط مدیر به این بخش دسترسی دارد.\n");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n--- پنل مدیریت کاربران ---");
            System.out.println("1) لیست کاربران");
            System.out.println("2) افزودن کاربر جدید");
            System.out.println("3) حذف کاربر");
            System.out.println("0) بازگشت به منوی اصلی");
            System.out.print("انتخاب شما: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> listUsers();
                case "2" -> addUser();
                case "3" -> removeUser();
                case "0" -> back = true;
                default -> System.out.println("گزینه نامعتبر است.\n");
            }
        }
    }

    private static void listUsers() {
        try {
            List<User> users = userService.listUsers(currentUser);
            System.out.println("\n--- لیست کاربران ---");
            if (users.isEmpty()) {
                System.out.println("هیچ کاربری در دیتابیس ثبت نشده.");
            } else {
                users.forEach(u -> System.out.println(u.getId() + ". " + u.getUsername() + " (" + u.getRole() + ")"));
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("خطا: " + e.getMessage() + "\n");
        }

    }


    private static void addUser() {
        System.out.print("نام کاربری: ");
        String username = scanner.nextLine().trim();
        System.out.print("رمز عبور: ");
        String password = scanner.nextLine().trim();
        System.out.println("نقش (1: Admin / 2: Warehouse Keeper / 3: Inspector): ");
        String roleChoice = scanner.nextLine().trim();
        Role role = switch (roleChoice) {
            case "1" -> ADMIN;
            case "2" -> WAREHOUSE_KEEPER;
            case "3" -> INSPECTOR;
            default -> null;
        };
        if (role == null) {
            System.out.println("نقش نامعتبر است.\n");
            return;
        }
        try {
            userService.addUser(username, password, role, currentUser);
            System.out.println("کاربر با موفقیت اضافه شد.\n");

        } catch (Exception e) {
            System.out.println("خطا: " + e.getMessage() + "\n");
        }
    }

    private static void removeUser() {
        listUsers();
        System.out.print("شناسه (id) کاربری که می‌خواهید حذف کنید: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            userService.deleteUser(id, currentUser);
            System.out.println(" کاربر حذف شد.\n");
        } catch (NumberFormatException e) {
            System.out.println("⚠ لطفاً یک عدد معتبر وارد کنید.\n");
        } catch (Exception e) {
            System.out.println("خطا: " + e.getMessage() + "\n");
        }
    }

    //------------------  ^ ^ ----------------------------------------------------------  ^ ^ ----------------------------
    private static void printProductList(List<Product> products, String title) {
        System.out.println("\n--- " + title + " ---");
        if (products.isEmpty()) {
            System.out.println("کالایی یافت نشد.");
        } else {
            for (Product p : products) {
                StringBuilder line = new StringBuilder();
                line.append(p.getId()).append(". ").append(p.getName())
                        .append(" [").append(p.getCode()).append("] - موجودی: ").append(p.getQuantity())
                        .append(" - وضعیت: ").append(p.getStatus());
                if (currentUser.canEditStock()) {
                    line.append(" - قیمت فروش: ").append(p.getSellPrice());
                }
                System.out.println(line);
            }
        }
        System.out.println();
    }

    @MenuCommand(name = "فیلتر بر اساس بازه‌ی قیمت", rowNumber = 11)
    private static void filterByPriceRange() {

        try {
            System.out.print("حداقل قیمت فروش: ");
            double min = Double.parseDouble(scanner.nextLine().trim());
            System.out.println("حداکثر قیمت فروش");
            double max = Double.parseDouble(scanner.nextLine().trim());

            List<Product> results = wareHouseService.findProductsByPriceRange(min, max);
            printProductList(results, "نتایج فیلتر قیمت بین " + min + " تا " + max);
        } catch (NumberFormatException e) {
            System.out.println("⚠ لطفاً یک عدد معتبر وارد کنید.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("خطا: " + e.getMessage() + "\n");
        }
    }

    @MenuCommand(name = "فیلتر بر اساس وضعیت", rowNumber = 12)
    private static void filterByStatus() {
        System.out.print("وضعیت مورد نظر (1: موجود / 2: ناموجود): ");
        String choice = scanner.nextLine().trim();
        ProductStatus status = choice.equals("2") ? ProductStatus.OUT_OF_STOCK : ProductStatus.AVAILABLE;

        List<Product> results = wareHouseService.findProductsByStatus(status);
        printProductList(results, "نتایج فیلتر وضعیت: " + status);


    }

    @MenuCommand(name = "ویرایش کالا", rowNumber = 9, selectedRole = {Role.ADMIN, Role.WAREHOUSE_KEEPER})

    private static void editProduct() {
        if (!currentUser.canEditStock()) {
            System.out.println("\n شما اجازه ویرایش کالا را ندارید.\n");
            return;
        }

        System.out.print("شناسه (id) یا کد کالا برای ویرایش: ");
        Product product = resolveProduct(scanner.nextLine().trim());
        if (product == null) {
            System.out.println("⚠ کالایی با این شناسه یا کد پیدا نشد.\n");
            return;
        }

        System.out.println("برای نگه‌داشتن مقدار فعلی، فقط Enter بزنید.");

        System.out.print("نام کالا [" + product.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = product.getName();
        }

        System.out.print("کد کالا [" + product.getCode() + "]: ");
        String code = scanner.nextLine().trim();
        if (code.isEmpty()) {
            code = product.getCode();
        }
        System.out.print("دسته‌بندی [" + product.getCategory() + "]: ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) {
            category = product.getCategory();
        }

        System.out.print("قیمت خرید [" + product.getPurchasePrice() + "]: ");
        String purchaseInput = scanner.nextLine().trim();
        double purchasePrice = purchaseInput.isEmpty() ? product.getPurchasePrice() : Double.parseDouble(purchaseInput);    //  if i use the (if_else) i must wrapped this in purchase price Double  but use the ternary operation is easy

        System.out.print("قیمت فروش [" + product.getSellPrice() + "]: ");
        String sellInput = scanner.nextLine().trim();
        double sellPrice = sellInput.isEmpty() ? product.getSellPrice() : Double.parseDouble(sellInput);   //same as top scope

        System.out.print("حداقل موجودی مجاز [" + product.getMinStockLevel() + "]: ");
        String minInput = scanner.nextLine().trim();
        int minStockLevel = minInput.isEmpty() ? product.getMinStockLevel() : Integer.parseInt(minInput);


        try {
            wareHouseService.updateProduct(product.getId(), name, code, category, purchasePrice, sellPrice, minStockLevel, currentUser);
            System.out.println("✅ کالا با موفقیت ویرایش شد.\n");
        } catch (Exception e) {
            System.out.println("خطا: " + e.getMessage() + "\n");

        }
    }

    @MenuCommand(name = "حذف کالا", rowNumber = 10, selectedRole = {Role.ADMIN})

    private static void deleteProduct() {
        if (!currentUser.canEditStock()) {
            System.out.println("\n شما اجازه حذف کالا را ندارید.\n");
            return;
        }
        System.out.print("شناسه (id) یا کد کالا برای حذف: ");
        Product product = resolveProduct(scanner.nextLine().trim());
        if (product == null) {
            System.out.println(" کالایی با این شناسه یا کد پیدا نشد.\n");
            return;
        }
        System.out.print("آیا از حذف «" + product.getName() + "» مطمئنید؟ (بله/خیر): ");
        if (!scanner.nextLine().trim().equals("بله")) {
            System.out.println("حذف لغو شد.\n");
            return;
        }

        try {
            wareHouseService.deleteProduct(product.getId(), currentUser);
            System.out.println(" کالا حذف شد.\n");
        } catch (Exception e) {
            System.out.println("خطا: " + e.getMessage() + "\n");
        }

    }

    @MenuCommand(name = "جستجو بر اساس دسته‌بندی", rowNumber = 8)

    private static void searchByCategory() {
        List<String> categories = wareHouseService.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("\nهیچ کالایی در انبار ثبت نشده.\n");
            return;
        }
        System.out.println("\nدسته‌بندی‌های موجود: " + String.join(", ", categories));
        System.out.print("دسته‌بندی مورد نظر برای جستجو: ");
        String category = scanner.nextLine().trim();

        List<Product> results = wareHouseService.findProductsByCategory(category);
        System.out.println("\n--- نتایج جستجو در دسته‌بندی «" + category + "» ---");
        if (results.isEmpty()) {
            System.out.println("کالای با این دسته بندی وجود ندارد ");
        } else {

            for (Product p : results) {
                StringBuilder line = new StringBuilder();
                line.append(p.getId()).append(". ").append(p.getName())
                        .append(" [").append(p.getCode()).append("] - موجودی: ").append(p.getQuantity())
                        .append(" - وضعیت: ").append(p.getStatus());
                if (currentUser.canEditStock()) {
                    line.append(" - قیمت فروش: ").append(p.getSellPrice());
                }
                System.out.println(line);
            }
        }


    }


//    private static void printMenu() {                                             //با وجود کلاس انوتیشن بهش دیگه نیازی نیست
//        System.out.println("---------------------------------");
//        System.out.println("1) مشاهده لیست کالاها");
//        System.out.println("2) ثبت کالای جدید");
//        System.out.println("3) ثبت فروش کالا");
//        System.out.println("4) ثبت خرید (افزایش موجودی)");
//        System.out.println("5) مشاهده هشدار موجودی کم");
//        System.out.println("6) مشاهده تاریخچه تراکنش‌ها");
//        System.out.println("7) گزارش نهایی (ارزش انبار + پرفروش‌ها)");
//        System.out.println("8) جستجوی کالا بر اساس دسته‌بندی");
//        System.out.println("9) ویرایش کالا");
//        System.out.println("10) حذف کالا");
//        System.out.println("11) فیلتر بر اساس بازه‌ی قیمت");
//        System.out.println("12) فیلتر بر اساس وضعیت (موجود/ناموجود)");
//        if (currentUser.getRole() == ADMIN) {
//            System.out.println("13) پنل مدیریت کاربران (Admin)");
//        }
//        System.out.println("0) خروج");
//        System.out.print("انتخاب شما: ");
//    }

    private static ProductRepository ChooseRepository() {

        System.out.println("داده‌های کالا کجا ذخیره شوند؟");
        System.out.println("1) فقط در حافظه (In-Memory) - با بستن برنامه پاک می‌شود");
        System.out.println("2) فایل JSON (products.json) - پایدار و خواناست");
        System.out.println("3) Java Serialization (products.dat) - پایدار و باینری است");
        System.out.print("انتخاب شما: ");
        String choice = scanner.nextLine().trim();
        return switch (choice) {

            case "2" -> {
                System.out.println("→ در حال استفاده از ProductJsonRepository (products.json)\n");
                yield new ProductJsonDbRepository(new ProductJsonRepository("products.json"));
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

    private static User login() {
        System.out.println(" اطلاعات کاربری خود را وارد کنید ");

        System.out.println("username :");
        String username = scanner.nextLine().trim();
        System.out.println("password :");
        String password = scanner.nextLine().trim();

        User user = AuthService.login(username, password);

        if (user != null) {
            System.out.println(
                    "Welcome " + user.getUsername());
            return user;
        }
        System.out.println("Username or Password is incorrect.");
        return null;
    }

    private static void initialProductSeed() {

        wareHouseService.addProduct(new Product(nextProductId++, "لپ‌تاپ ایسوس", "LP-001", "الکترونیک", 25000000, 29000000, 8, 3),
                new Admin(0, "system", "system"));
        wareHouseService.addProduct(
                new Product(nextProductId++, "ماوس لاجیتک", "MS-002", "لوازم جانبی", 350000, 450000, 2, 5),
                new Admin(0, "system", "system"));

        wareHouseService.addProduct(
                new Product(nextProductId++, "کیبورد مکانیکال", "KB-003", "لوازم جانبی", 900000, 1200000, 15, 4),
                new Admin(0, "system", "system"));

    }

    @MenuCommand(name = "مشاهده لیست کالا ها", rowNumber = 1)
    private static void viewAllProducts() {
        List<Product> products = wareHouseService.getAllProducts();
        System.out.println("\n--- لیست کالاها ---");
        for (Product p : products) {
            StringBuilder line = new StringBuilder();
            line.append(p.getId()).append(". ").append(p.getName())
                    .append(" [").append(p.getCode()).append("] - موجودی: ").append(p.getQuantity())
                    .append(" - وضعیت: ").append(p.getStatus());
            if (currentUser.canEditStock()) {
                line.append(" - قیمت فروش: ").append(p.getSellPrice());

            }
            System.out.println(line);
        }
        System.out.println();
    }

    @MenuCommand(name = "ثبت کالای جدید", rowNumber = 2, selectedRole = {ADMIN, WAREHOUSE_KEEPER})
    private static void addProduct() {
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

    @MenuCommand(name = "ثبت فروش کالا", rowNumber = 3)
    private static void sellProduct() {
        System.out.print("شناسه (id) کالا برای فروش: ");
        String input = scanner.nextLine().trim();
        Product product = resolveProduct(input);
        if (product == null) {
            System.out.println("⚠ کالایی با این شناسه یا کد پیدا نشد.\n");
            return;
        }
        System.out.print("تعداد فروش: ");
        int qty = Integer.parseInt(scanner.nextLine());
        try {
            wareHouseService.sellProduct(product.getId(), qty, currentUser);
            System.out.println("فروش با موفقیت ثبت شد"); //////////////
        } catch (Exception e) {
            System.out.println("خطا: " + e.getMessage() + "\n");
        }

    }


    private static Product resolveProduct(String input) {
        try {
            int id = Integer.parseInt(input);
            return wareHouseService.getAllProducts().stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);
        } catch (NumberFormatException e) {
            // ورودی عدد نبود، پس به‌عنوان کد کالا جستجو می‌کنیم
            return wareHouseService.findProductByCode(input).orElse(null);
        }
    }

    @MenuCommand(name = "ثبت خرید (افزایش موجودی)", rowNumber = 4, selectedRole = {ADMIN, WAREHOUSE_KEEPER})
    private static void purchaseProduct() {
        if (!currentUser.canEditStock()) {
            System.out.println("\n شما اجازه ثبت خرید ندارید.\n");
            return;
        }
        System.out.print("شناسه (id) کالا برای افزایش موجودی: ");

        String input = scanner.nextLine().trim();
        Product product = resolveProduct(input);
        if (product == null) {
            System.out.println("⚠ کالایی با این شناسه یا کد پیدا نشد.\n");
            return;
        }

        System.out.print("تعداد خرید: ");
        int qty = Integer.parseInt(scanner.nextLine());
        try {
            wareHouseService.purchaseProduct(product.getId(), qty, currentUser);
            System.out.println(" خرید با موفقیت ثبت شد.\n");

        } catch (Exception e) {
            System.out.println("خطا: " + e.getMessage() + "\n");

        }
    }

    @MenuCommand(name = "مشاهده هشدار موجودی کم", rowNumber = 5)
    private static void viewLowStock() {

        List<Product> lowStock = AlertService.checkLowStock(wareHouseService.getAllProducts());
        System.out.println("\n--- کالاهای با موجودی کم ---");
        if (lowStock.isEmpty()) {
            System.out.println("everything just fine ! ...");

        } else {
            lowStock.forEach(p -> System.out.println("⚠ " + p.getName() + " - باقی‌مانده: " + p.getQuantity()));
        }
        System.out.println();
    }

    @MenuCommand(name = "مشاهده تاریخچه تراکنش ها", rowNumber = 6)
    private static void viewTransactionHistory() {
        if (!currentUser.canViewReports() && currentUser.getRole() != WAREHOUSE_KEEPER) {
            System.out.println("\n شما اجازه مشاهده تاریخچه را ندارید.\n");
            return;
        }
        List<Transaction> history = wareHouseService.getTransactionHistory();
        System.out.println("\n--- تاریخچه تراکنش‌ها ---");
        if (history.isEmpty()) {
            System.out.println("(هنوز هیچ تراکنشی در دیتابیس ثبت نشده - این بخش با اتصال واقعی JDBC کار می‌کند)");
        } else {
            history.forEach(System.out::println);
        }
        System.out.println();
    }

    @MenuCommand(name = "گزارش نهایی (ارزش انبار + پرفروش ها)", rowNumber = 7, selectedRole = {ADMIN, INSPECTOR})
    private static void generateAndExportReport() {
        if (!currentUser.canViewReports()) {
            System.out.println("\n شما اجازه مشاهده گزارش‌ها را ندارید.\n");
            return;
        }
        List<Product> products = wareHouseService.getAllProducts();
        List<Transaction> transactions = wareHouseService.getTransactionHistory();
        ReportData reportData = ReportService.generateReport(products, transactions);

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
