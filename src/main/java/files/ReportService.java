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
    public static List<String> getBestSellingProducts(List<Transaction> transactions, int topN) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.SELL)
                .collect(Collectors.groupingBy(
                        t -> t.getProduct().getName(),
                        Collectors.summingInt(Transaction::getQuantity)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
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
