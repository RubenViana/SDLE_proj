package shoppingList.client.helper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Connections {

    private static final ZContext context = new ZContext();
    private static final List<Integer> routersPorts = List.of(5000); //ONLY ONE ROUTER FOR NOW
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
        // TODO: add item to the item list and them update the list in the database

        //Stub for now
        return updateListDB(databaseURL, listID, item);
    }

    public static boolean removeItemDB(String databaseURL, String listID, String item) {

        // TODO: remove item from the item list and them update the list in the database

        //Stub for now
        return updateListDB(databaseURL, listID, null);
    }

    public static boolean updateItemDB(String databaseURL, String listID, String item) {
        // TODO: update item from the item list and them update the list in the database

        return updateListDB(databaseURL, listID, item);
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

    public static ZMQ.Socket establishConnectionToRouter() {

        //loop for all routers address know to the client, connect to the first one that is available
        for (int port : routersPorts) {
            System.out.println("Connecting to router at port " + port);

            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://localhost:" + port);
            String clientID = "client_" + System.currentTimeMillis();
            socket.setIdentity(clientID.getBytes());

            //send status frame
            socket.send(new Gson().toJson(new Frame(Frame.FrameStatus.CLIENT_OK, Frame.FrameAction.ROUTER_STATUS, "")));

            //receive status frame
            Frame response = new Gson().fromJson(socket.recvStr(), Frame.class);

            switch (response.getStatus()) {
                case ROUTER_OK:
                    System.out.println("Connected to router at port " + port);
                    return socket;
                case ROUTER_ERROR:
                    System.out.println("Error connecting to router at port " + port);
                    break;
                default:
                    break;
            }
        }
        System.out.println("No router available");
        return null;
    }

    public static boolean pullListFromServer(String databaseURL, String listID) {

        ZMQ.Socket socket = establishConnectionToRouter();

        if (socket == null) {
            return false;
        }

        //send pull list frame
        socket.send(new Gson().toJson(new Frame(Frame.FrameStatus.CLIENT_OK, Frame.FrameAction.PULL_LIST, listID)));

        //receive pull list frame
        Frame response = new Gson().fromJson(socket.recvStr(), Frame.class);

        switch (response.getStatus()) {
            case SERVER_OK:
                System.out.println("List pulled from server");
                //TODO update list in database
                return true;
            case SERVER_ERROR:
                System.out.println("Error pulling list from server");
                break;
            default:
                break;
        }

        return false;
    }
}
