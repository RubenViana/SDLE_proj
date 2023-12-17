package shoppingList.server.helper;

import com.google.gson.Gson;
import org.zeromq.ZMQ;
import shoppingList.server.helper.Frame;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Connections {
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public static boolean sendFrameRouter(ZMQ.Socket socket, String clientAddress, Frame frame) {
        return socket.sendMore(clientAddress) && socket.sendMore("") && socket.send(new Gson().toJson(frame));
    }

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

    public static boolean addListDB(String databaseURL, String listID) {
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "INSERT INTO lists (list_id) VALUES (?)";
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

    public static String getItemsDB(String databaseURL, String listID) {

        //This is not complete until CRDT is implemented
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "SELECT * FROM lists WHERE list_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, listID);
                try (ResultSet rs = stmt.executeQuery()) {
                    StringBuilder sb = new StringBuilder();
                    while (rs.next()) {
                        sb.append(rs.getString("item"));
                        sb.append("\n");
                    }
                    return sb.toString();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return null;
    }

    public static boolean updateListDB(String databaseURL, String listID, String item) {
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "UPDATE lists SET item = ? WHERE list_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, item);
                stmt.setString(2, listID);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return false;
    }
}
