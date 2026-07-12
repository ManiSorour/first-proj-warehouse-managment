package files;

import java.io.Serializable;
import java.util.List;

public class ReportData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String generatedAt;
    private double totalInventoryValue;
    private List<String> bestSellingProductNames;
    private int totalProductCount;


    public ReportData(String generatedAt, double totalInventoryValue, List<String> bestSellingProductNames, int totalProductCount) {
        this.generatedAt = generatedAt;
        this.totalInventoryValue = totalInventoryValue;
        this.bestSellingProductNames = bestSellingProductNames;
        this.totalProductCount = totalProductCount;
    }

    public ReportData(double totalValue, List<String> bestSellers, int size) {
        this.generatedAt = java.time.LocalDateTime.now().toString();
        this.totalInventoryValue = totalValue;
        this.bestSellingProductNames = bestSellers;
        this.totalProductCount = size;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public double getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public List<String> getBestSellingProductNames() {
        return bestSellingProductNames;
    }

    public int getTotalProductCount() {
        return totalProductCount;
    }


    @Override
    public String toString() {
        return "گزارش انبار - تاریخ تولید: " + generatedAt +
                "\nارزش کل موجودی: " + totalInventoryValue +
                "\nتعداد کل کالاها: " + totalProductCount +
                "\nپرفروش‌ترین کالاها: " + bestSellingProductNames;
    }
}
