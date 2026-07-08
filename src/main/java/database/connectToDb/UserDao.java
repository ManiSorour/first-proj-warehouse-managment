package database.connectToDb;

import database.configuration.Dao;
import database.configuration.DataBaseConnection;
import model.role.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao implements Dao<User> {
    @Override
    public User findById(int id) {

        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1 , id);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next())
                    return mapRow(rs);
                else {
                    return null ;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    private User mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("passwordHash");
        Role role = Role.valueOf(rs.getString("role"));

        return switch (role){
        case ADMIN -> new Admin(id , username , passwordHash);
        case WAREHOUSE_KEEPER -> new InventoryManager(id , username , passwordHash);
        case INSPECTOR -> new Inspector(id , username , passwordHash);

        };
    }



    private User findByUsername (String username){                              // نوشتمش چونکه صفحه لاگین نیاز هست به یوزر و پسورد و نمیتونی با ایدی کاربر رو لاگین کنی

        String sql = "SELECT * FROM users WHERE username = ?";

        try(Connection conn = DataBaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1 , username);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("خطا در findByUsername: " + e.getMessage());

        }
        return null;
    }



    @Override
    public List<User> findAll() {

        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()){
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("خطا در findAll: " + e.getMessage());
        }

        return users;
    }

    @Override
    public void save(User u) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1 , u.getUsername());
            stmt.setString(2 , u.getPasswordHash());
            stmt.setString(3 , u.getRole().name()) ;
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("خطا در save: " + e.getMessage());
        }
    }

    @Override
    public void update(User u) {

        String sql = "UPDATE users SET username=?, password_hash=?, role=? WHERE id=?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1 , u.getUsername());
            stmt.setString(2 , u.getPasswordHash());
            stmt.setString(3 , u.getRole().name()) ;
            stmt.setInt(4 , u.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("خطا در update: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();


        } catch (SQLException e) {
            System.err.println("خطا در delete: " + e.getMessage());
        }

    }
}
