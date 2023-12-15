package shoppingList.helper;

import java.sql.*;
import java.util.ArrayList;

public class Connections {

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

    public static boolean doesItemExistDB(String databaseURL, String listID, String item) {
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "SELECT * FROM lists WHERE list_id = ? AND item = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, listID);
                stmt.setString(2, item);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return false;
    }

    public static boolean addItemDB(String databaseURL, String listID, String item) {
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "UPDATE lists SET item = ? WHERE list_id = ?";
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

    public static boolean removeItemDB(String databaseURL, String listID, String item) {
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "UPDATE lists SET item = NULL WHERE list_id = ?";
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

    public static ArrayList<String> getListsDB(String databaseURL) {
        ArrayList<String> lists = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(databaseURL);
            String query = "SELECT * FROM lists";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        lists.add(rs.getString("list_id"));
                    }
                    return lists;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
        return null;
    }
}
