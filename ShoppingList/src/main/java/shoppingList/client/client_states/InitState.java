package shoppingList.client.client_states;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

import static java.lang.System.exit;

public class InitState implements ClientState {
    private String userID;
    private String databaseURL;

    public InitState(String userID) {
        this.userID = userID;
    }
    @Override
    public ClientState run() {

        if (connectOrCreateDatabase()) {
            System.out.println("Connected to database");
            return new MainMenuState(this.databaseURL);
        }
        else {
            System.out.println("Failed to connect to database");
        }

        return null;
    }

    private boolean connectOrCreateDatabase() {
        Connection connection;
        this.databaseURL = "jdbc:sqlite:./src/main/java/shoppingList/client/" + this.userID + ".db";

        try {
            // Attempt to connect to the database
            connection = DriverManager.getConnection(this.databaseURL);

            File file = new File("client/" + this.userID + ".db");
            if (!file.exists()) {
                // Create a table (if it doesn't exist)
                createTable(connection);
            }
            return true;

        } catch (SQLException e) {
            // Handle database connection errors
            System.err.println("Error connecting to the database: " + e.getMessage());
        }

        return false;
    }

    private static void createTable(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            // Create a simple table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS lists (list_id TEXT PRIMARY KEY, item TEXT)";
            statement.executeUpdate(createTableSQL);

            System.out.println("Table created or already exists");

        } catch (SQLException e) {
            // Handle table creation errors
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
}
