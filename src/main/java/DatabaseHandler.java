import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {
    public Connection getConnection() {

        Connection conn = null;
        String url = "jdbc:mysql://127.0.0.1:3306/mgschcht";
        String username = "root";
        String password = "root";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return conn;
    }
}

