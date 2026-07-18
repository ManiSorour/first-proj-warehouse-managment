package database.connectToDb;

import database.configuration.GenericRepository;
import database.configuration.DataBaseConnection;
import model.product.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductGenericRepository implements GenericRepository<Product> {
    @Override
    public Product findById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("خطا در findById: " + e.getMessage());
        }
        return null;
    }


    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM Products ORDER BY id";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("خطا در findAll: " + e.getMessage());
        }

        return products;

    }

    @Override
    public void save(Product p) {

        String sql = "INSERT INTO products (name, code, category, purchase_price, sell_price, quantity, min_stock_level) " +
                "VALUES (?, ?, ?, ?, ?, ?, ? )";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getName());
            stmt.setString(2, p.getCode());
            stmt.setString(3, p.getCategory());
            stmt.setDouble(4, p.getPurchasePrice());
            stmt.setDouble(5, p.getSellPrice());
            stmt.setInt(6, p.getQuantity());
            stmt.setInt(7, p.getMinStockLevel());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("خطا در save: " + e.getMessage());
        }
    }

    @Override
    public void update(Product p) {

        String sql = "UPDATE products SET name=?, code=?, category=?, purchase_price=?, " +
                "sell_price=?, quantity=?, min_stock_level=? WHERE id=?";


        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getName());
            stmt.setString(2, p.getCode());
            stmt.setString(3, p.getCategory());
            stmt.setDouble(4, p.getPurchasePrice());
            stmt.setDouble(5, p.getSellPrice());
            stmt.setInt(6, p.getQuantity());
            stmt.setInt(7, p.getMinStockLevel());
            stmt.setInt(8, p.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("خطا در update" + e.getMessage());
        }
    }


    public void update(Connection conn , Product p){

        String sql = "UPDATE products SET name=?, code=?, category=?, purchase_price=?, " +
                "sell_price=?, quantity=?, min_stock_level=? WHERE id=?";


        try (PreparedStatement stmt =conn.prepareStatement(sql)){


            stmt.setString(1, p.getName());
            stmt.setString(2, p.getCode());
            stmt.setString(3, p.getCategory());
            stmt.setDouble(4, p.getPurchasePrice());
            stmt.setDouble(5, p.getSellPrice());
            stmt.setInt(6, p.getQuantity());
            stmt.setInt(7, p.getMinStockLevel());
            stmt.setInt(8, p.getId());
            stmt.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }









    @Override
    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id= ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setInt(1, id);
            stmt.executeUpdate();


        } catch (SQLException e) {
            System.err.println("خطا در delete: " + e.getMessage());
        }
    }

    public Product findByCode(String code) {       // برای اینکه کد تکراری نداشته باشیم
        String sql = "SELECT * FROM products WHERE code = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("خطا در findByCode: " + e.getMessage());
        }
        return null;
    }


    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("code"),
                rs.getString("category"),
                rs.getDouble("purchase_price"),
                rs.getDouble("sell_price"),
                rs.getInt("quantity"),
                rs.getInt("min_stock_level")
        );
        return p;
    }
}
