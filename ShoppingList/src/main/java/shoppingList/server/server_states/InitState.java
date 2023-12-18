package shoppingList.server.server_states;

import shoppingList.server.helper.Connections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class InitState implements ServerState{
    private final int serverID;
    private String databaseURL;

    public InitState(int serverID) {
        this.serverID = serverID;
    }

    @Override
    public ServerState run() {

        if (connectOrCreateDatabase()) {
            Connections.logEvent("Connected to database", "{Database: " + this.databaseURL + "}");

            return new ConnectRouterState(this.serverID, this.databaseURL);
        }
        else {
            Connections.logEvent("Failed to connect to database", "{Database: " + this.databaseURL + "}");
        }

        return null;
    }

    private boolean connectOrCreateDatabase() {
        try {

            Connection connection;
            this.databaseURL = "jdbc:sqlite:./src/servers/server_" + this.serverID + "_database.db";
            // Attempt to connect to the database
            connection = DriverManager.getConnection(this.databaseURL);
            createTable(connection);

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

        } catch (SQLException e) {
            // Handle table creation errors
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
}
