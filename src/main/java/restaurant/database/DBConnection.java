package restaurant.database;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final Dotenv dotenv;

    static {
        dotenv = Dotenv.load();

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL non trouvé", e);
        }
    }

    public Connection getDBConnection() throws SQLException {
        String url = dotenv.get("DB_URL");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        return DriverManager.getConnection(url, user, password);
    }

}
