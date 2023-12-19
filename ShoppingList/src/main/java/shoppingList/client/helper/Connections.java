package shoppingList.client.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import shoppingList.client.helper.CRDT;
import shoppingList.client.helper.Item;

public class Connections {
    public static final Integer PULLING_RATE = 30; //seconds

    private static final int TIMEOUT = 2000; //timeout for receiving a response from the router
    private static final ZContext context = new ZContext();
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
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
            String query = "INSERT INTO lists (list_id, item) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, listID);
                stmt.setString(2, "[]");
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

    public static boolean updateListDB(String databaseURL, String userID, String listID, String item) {

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

    public static boolean doesItemExistDB(String databaseURL, String listID, String item) {
        String items = getItemsDB(databaseURL, listID);
        CRDT crdt = new CRDT(items);
        return crdt.getItem(item) != null;
    }

    public static boolean addItemDB(String databaseURL, String userID, String listID, String item, int quantity) {
        String items = getItemsDB(databaseURL, listID);
        CRDT crdt = new CRDT(items);

        crdt.addOrUpdateItem(new Item(item, quantity, System.currentTimeMillis()));
        String updatedItems = crdt.getItemsList();

        return updateListDB(databaseURL, userID, listID, updatedItems);
    }

    public static boolean removeItemDB(String databaseURL, String userID, String listID, String item) {

        String items = getItemsDB(databaseURL, listID);
        CRDT crdt = new CRDT(items);

        crdt.removeItem(new Item(item, 0, System.currentTimeMillis()));
        String updatedItems = crdt.getItemsList();

        return updateListDB(databaseURL, userID, listID, updatedItems);
    }

    public static boolean updateItemDB(String databaseURL, String userID, String listID, String item, int quantity) {

        String items = getItemsDB(databaseURL, listID);
        CRDT crdt = new CRDT(items);

        crdt.addOrUpdateItem(new Item(item, quantity, System.currentTimeMillis()));
        String updatedItems = crdt.getItemsList();

        return updateListDB(databaseURL, userID, listID, updatedItems);
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

    public static ZMQ.Socket establishConnectionToRouter(String userID) {

        //loop for all routers address know to the client, connect to the first one that is available
        for (int port : routersPorts) {
            logEvent(userID, "Connecting to router at port " + port);

            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://localhost:" + port);
            String clientID = userID + "_" + UUID.randomUUID().toString().substring(0, 4);
            socket.setIdentity(clientID.getBytes());

            //send status frame
            socket.send(new Gson().toJson(new Frame(Frame.FrameStatus.CLIENT_OK, Frame.FrameAction.ROUTER_STATUS, "", "")));

            // Use ZMQ.Poller to wait for events on the socket with a timeout
            ZMQ.Poller poller = context.createPoller(1);
            poller.register(socket, ZMQ.Poller.POLLIN);

            long startTime = System.currentTimeMillis();

            while (poller.poll(TIMEOUT) != -1) {
                if (System.currentTimeMillis() - startTime > TIMEOUT) {
                    logEvent(userID, "Timeout connecting to router at port " + port);
                    break;
                }

                // Check if the socket has an event
                if (poller.pollin(0)) {
                    // Receive the response
                    Frame response = new Gson().fromJson(socket.recvStr(), Frame.class);
                    switch (response.getStatus()) {
                        case ROUTER_OK:
                            logEvent(userID, "Connected to router at port " + port);
                            poller.close();
                            return socket;
                        case ROUTER_ERROR:
                            logEvent(userID, "Error connecting to router at port " + port);
                            break;
                        default:
                            break;
                    }
                }
            }
            socket.close();
            poller.close();
        }
        logEvent(userID, "No router available");
        return null;
    }

    public static boolean pullListFromServer(String databaseURL, String userID, String listID) {

        ZMQ.Socket socket = establishConnectionToRouter(userID);

        if (socket == null) {
            return false;
        }

        //send pull list frame
        socket.send(new Gson().toJson(new Frame(Frame.FrameStatus.CLIENT_OK, Frame.FrameAction.PULL_LIST, listID, "")));

        // Use ZMQ.Poller to wait for events on the socket with a timeout
        ZMQ.Poller poller = context.createPoller(1);
        poller.register(socket, ZMQ.Poller.POLLIN);

        long startTime = System.currentTimeMillis();

        while (poller.poll(TIMEOUT) != -1) {
            if (System.currentTimeMillis() - startTime > TIMEOUT) {
                logEvent(userID, "Timeout waiting for router response");
                break;
            }

            if (poller.pollin(0)){
                //receive pull list frame
                Frame response = new Gson().fromJson(socket.recvStr(), Frame.class);

                switch (response.getStatus()) {
                    case SERVER_OK:
                        logEvent(userID, "List " + listID + " pulled from server");

                        //TESTING PURPOSES ONLY, NOT FINAL IMPLEMENTATION
                        if (!doesListExistDB(databaseURL, listID))
                            if (addListDB(databaseURL, listID))
                                logEvent(userID, "List " + listID + " added to database");

                        if (updateListDB(databaseURL, userID, listID, response.getListItem()))
                            logEvent(userID, "List " + listID + " updated in database");

                        poller.close();
                        socket.close();
                        return true;
                    case SERVER_ERROR:
                        logEvent(userID, "Error pulling list " + listID + " from server");
                        poller.close();
                        socket.close();
                        return false;
                    default:
                        break;
                }
            }
        }
        poller.close();
        socket.close();
        return false;
    }

    public static boolean pushListToServer(String databaseURL, String userID, String listID) {

        ZMQ.Socket socket = establishConnectionToRouter(userID);

        if (socket == null) {
            return false;
        }

        String item = getItemsDB(databaseURL, listID);

        //send push list frame
        socket.send(new Gson().toJson(new Frame(Frame.FrameStatus.CLIENT_OK, Frame.FrameAction.PUSH_LIST, listID, item)));

        // Use ZMQ.Poller to wait for events on the socket with a timeout
        ZMQ.Poller poller = context.createPoller(1);
        poller.register(socket, ZMQ.Poller.POLLIN);

        long startTime = System.currentTimeMillis();

        while (poller.poll(TIMEOUT) != -1) {
            if (System.currentTimeMillis() - startTime > TIMEOUT) {
                logEvent(userID, "Timeout waiting for router response");
                break;
            }

            if (poller.pollin(0)) {
                //receive push list frame
                Frame response = new Gson().fromJson(socket.recvStr(), Frame.class);

                switch (response.getStatus()) {
                    case SERVER_OK:
                        logEvent(userID, "List " + listID + " pushed to server");

                        poller.close();
                        socket.close();
                        return true;
                    case SERVER_ERROR:
                        logEvent(userID, "Error pushing list " + listID + " to server");

                        poller.close();
                        socket.close();
                        return false;
                    default:
                        break;
                }
            }
        }
        poller.close();
        socket.close();
        return false;
    }

    public static void updateLocalListsFromServer(String databaseURL, String userID) {
        ArrayList<String> lists = getListsDB(databaseURL);
        if (lists == null) {
            return;
        }

        for (String list : lists) {
            pullListFromServer(databaseURL, userID, list);
        }
    }

    private static void logEvent(String userID, String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("./src/users/" + userID + "_logs.txt", true))) {
            String timestamp = TIMESTAMP_FORMAT.format(new Date());
            writer.printf("[%s]   %s%n", timestamp, message);
        } catch (IOException e) {
            try {
                PrintWriter writer = new PrintWriter("./src/users/" + userID + "_logs.txt");
                writer.close();
            } catch (IOException ex) {
                System.err.println("Creating Log File " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public static void updateServerListsFromLocal(String databaseURL, String userID) {
        ArrayList<String> lists = getListsDB(databaseURL);
        if (lists == null) {
            return;
        }

        for (String list : lists) {
            pushListToServer(databaseURL, userID, list);
        }
    }
}
