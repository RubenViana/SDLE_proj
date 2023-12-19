package shoppingList.server.helper;

import com.google.gson.Gson;
import org.zeromq.ZMQ;
import shoppingList.server.helper.CRDT;
import shoppingList.server.helper.Frame;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class Connections {
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final List<Integer> routersPorts = List.of(5000, 5001); //ONLY ONE ROUTER FOR NOW

    public static final Integer SERVER_PORT = 7000;

    public static void logEvent(String event, String details) {
        String timestamp = TIMESTAMP_FORMAT.format(new Date());
        System.out.printf("[%-8s]   %-37s %s%n", timestamp, event, details);
    }

    public static boolean doesListExistDB(String databaseURL, String listID) {
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "SELECT * FROM lists WHERE list_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, listID);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return false;
    }

    public static boolean addListDB(String databaseURL, String listID, String item) {
        if (Objects.equals(item, "")) item = "[]";
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "INSERT INTO lists (list_id, item) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, listID);
                stmt.setString(2, item);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return false;
    }

    public static boolean removeListDB(String databaseURL, String listID) {
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "DELETE FROM lists WHERE list_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, listID);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return false;
    }

    public static boolean updateListDB(String databaseURL, String listID, String item) {
        if (Objects.equals(item, "")) item = "[]";
        String oldItems = getItemsDB(databaseURL, listID);
        CRDT crdt = new CRDT(oldItems);
        crdt.merge(item);

        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "UPDATE lists SET item = ? WHERE list_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, crdt.getItemsList());
                stmt.setString(2, listID);
                stmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return false;
    }

    public static String getItemsDB(String databaseURL, String listID) {
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "SELECT * FROM lists WHERE list_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, listID);
                try (ResultSet rs = stmt.executeQuery()) {
                    StringBuilder sb = new StringBuilder();
                    while (rs.next()) {
                        sb.append(rs.getString("item"));
                    }
                    return new CRDT(sb.toString()).getItemsList();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return null;
    }

}
