package shoppingList.client.client_states;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import shoppingList.client.helper.Connections;

import static java.lang.System.exit;

public class InitState implements ClientState {
    private final String userID;
    private String databaseURL;

    public InitState(String userID) {
        this.userID = userID;
    }
    @Override
    public ClientState run() {


        if (connectOrCreateDatabase()) {
            System.out.println("Connected to database");

            // Create a ScheduledExecutorService with a single thread
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            // Pull local lists from the server every 10 seconds
            scheduler.scheduleAtFixedRate(() -> Connections.updateLocalListsFromServer(this.databaseURL, this.userID), 0, Connections.PULLING_RATE, TimeUnit.SECONDS);

            return new MainMenuState(this.databaseURL, this.userID);
        }
        else {
            System.out.println("Failed to connect to database");
        }

        return null;
    }

    private boolean connectOrCreateDatabase() {
        try {
            File file = new File("./src/users/" + this.userID + "_database.db");

            Connection connection;
            this.databaseURL = "jdbc:sqlite:./src/users/" + this.userID + "_database.db";
            // Attempt to connect to the database
            connection = DriverManager.getConnection(this.databaseURL);

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
