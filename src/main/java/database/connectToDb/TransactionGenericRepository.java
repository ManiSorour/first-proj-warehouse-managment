package database.connectToDb;

import database.configuration.GenericRepository;
import database.configuration.DataBaseConnection;
import model.product.Product;
import model.transaction.Transaction;
import model.transaction.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionGenericRepository implements GenericRepository<Transaction> {

    private final ProductGenericRepository productDao = new ProductGenericRepository();

    @Override
    public Transaction findById(int id) {

        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return mapRow(rs);
                }
            }



        } catch (SQLException e) {
            System.err.println("خطا در findById: " + e.getMessage());
        }
        return null;
    }



        private Transaction mapRow(ResultSet rs) throws SQLException {
            int productId = rs.getInt("product_id");
            Product product = productDao.findById(productId);


            return new Transaction(
                    rs.getInt("id"),
                    product,
                    TransactionType.valueOf(rs.getString("type")),
                    rs.getInt("quantity"),
                    rs.getString("performed_by"),
                    rs.getTimestamp("date_time").toLocalDateTime()


            );
        }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date_time DESC";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {


            while (rs.next()){
                transactions.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("خطا در findAll: " + e.getMessage());

        }

        return transactions;
    }

    @Override
    public void save(Transaction t) {
        String sql = "INSERT INTO transactions (product_id, type, quantity, performed_by) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, t.getProduct().getId());
            stmt.setString(2, t.getType().name());
            stmt.setInt(3, t.getQuantity());
            stmt.setString(4, t.getPerformedByUsername());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("خطا در save: " + e.getMessage());
        }
    }

        @Override
    public void update(Transaction t) {

            throw new UnsupportedOperationException("تراکنش‌های ثبت‌شده قابل ویرایش نیستند");

        }

    @Override
    public void delete(int id) {

        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1,id);
            stmt.executeUpdate();

        } catch (SQLException e) {

            System.err.println("خطا در delete: " + e.getMessage());

        }

    }
}



