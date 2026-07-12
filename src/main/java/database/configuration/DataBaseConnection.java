package database.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class DataBaseConnection {

    private static final String URL =  "jdbc:postgresql://192.168.8.112:5432/warehouse";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static Connection connection ;


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
