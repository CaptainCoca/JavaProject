package JavaProject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    static String URL = "jdbc:mysql://localhost:3306/gestion_jeux";
    static String LOGIN = "root";
    static String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        Connection connexion = DriverManager.getConnection(URL, LOGIN, PASSWORD);
        return connexion;
    }
}