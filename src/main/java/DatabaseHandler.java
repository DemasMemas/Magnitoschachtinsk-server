import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {
    public Connection getConnection() {
        //String url = "jdbc:mysql://127.0.0.1:3306/mgschcht";
        //String username = "root";
        //String password = "root";

        Connection conn = null;
        String url = "jdbc:mysql://sql7.freesqldatabase.com:3306/sql7619394";
        String username = "sql7619394";
        String password = "bG8DalSj6g";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return conn;
    }
}

