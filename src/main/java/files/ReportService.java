package files;

import model.product.Product;
import model.transaction.Transaction;
import model.transaction.TransactionType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {



    // مجموع ارزش کل موجودی انبار (قیمت خرید * تعداد، برای همه کالاها)
    public static double calculateTotalInventoryValue(List<Product> products) {
        return products.stream()
                .mapToDouble(Product::getTotalValue)
                .sum();
    }

    // پرفروش‌ترین کالاها بر اساس مجموع تعداد فروخته‌شده در تاریخچه تراکنش‌ها
    public static List<String> getBestSellingProducts(List<Transaction> transactions, int top) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.SELL)
                .collect(Collectors.groupingBy(           // باید یه فور میذاشتم جاش گروپ بای گذاشتم تا بر اساس نام پیدا کنه موجودیو
                        t -> t.getProduct().getName(),
                        Collectors.summingInt(Transaction::getQuantity)
                ))
                .entrySet().stream()     // برای اینکه از مپ استریم بگیرم از این لاین استفاده میکمم
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(top)
                .map(entry -> entry.getKey() + " (" + entry.getValue() + " عدد فروخته‌شده)")
                .collect(Collectors.toList());
    }

    // ساخت آبجکت نهایی گزارش برای خروجی‌گیری (JSON/XML/txt/باینری)
    public static ReportData generateReport(List<Product> products, List<Transaction> transactions) {
        double totalValue = calculateTotalInventoryValue(products);
        List<String> bestSellers = getBestSellingProducts(transactions, 5);
        return new ReportData(totalValue, bestSellers, products.size());
    }


}
