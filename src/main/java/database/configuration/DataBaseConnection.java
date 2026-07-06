package database.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// حتما قبل اجرا برنامه کانفیگ اس کیو ال و کوئری هاشو بزنم!!!!!

public class DataBaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/warehouse_db";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password";

    private static Connection connection ;


    public DataBaseConnection() {
    }


    public static Connection getConnection() throws SQLException {

        if (connection == null || connection.isClosed()){
            connection = DriverManager.getConnection(URL , USER , PASSWORD);

        }
        return connection;

    }


    public static void closeConection() {
        try {
            if (connection != null && !connection.isClosed()) {

                connection.close();
            }

        } catch (SQLException e) {
            System.err.println("خطا در بستن اتصال دیتابیس: " + e.getMessage());
        }

    }
}
