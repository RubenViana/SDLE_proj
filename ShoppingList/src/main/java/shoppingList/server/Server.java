package shoppingList.server;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import shoppingList.client.client_states.MainMenuState;
import shoppingList.server.helper.Connections;
import shoppingList.server.helper.Frame;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Server {

    private final int id;
    private String databaseURL;
    private static final List<Integer> routersPorts = List.of(6000); //ONLY ONE ROUTER FOR NOW

    public Server(int id) {
        this.id = id;
    }


    private void init() {

        if (connectOrCreateDatabase()) {
            Connections.logEvent("Connected to database", "{Database: " + this.databaseURL + "}");
        }
        else {
            Connections.logEvent("Failed to connect to database", "{Database: " + this.databaseURL + "}");
        }

        for (int port : routersPorts) {
            try (ZContext context = new ZContext()) {
                ZMQ.Socket server = context.createSocket(SocketType.REQ);
                String serverId = "SERVER_" + this.id;
                server.setIdentity(serverId.getBytes());

                server.connect("tcp://localhost:" + port);

                //Send server status message to router, to later enter the ring
                Connections.sendFrameRouter(server, "", new Frame(Frame.FrameStatus.SERVER_OK, Frame.FrameAction.SERVER_STATUS, "", ""));
                Connections.logEvent("Connect to router", "{Router: " + port + "}");


                //Loop to receive requests from router
                while (!Thread.currentThread().isInterrupted()) {
                    String address = server.recvStr();
                    String empty = server.recvStr();
                    assert (empty.isEmpty());

                    Frame request = new Gson().fromJson(server.recvStr(), Frame.class);
                    Frame response;

                    switch (request.getAction()) {
                        case SERVER_STATUS:
                            Connections.logEvent("Reply to ", "{Server: " + this.id + "}");
                            break;
                        case PULL_LIST:
                            Connections.logEvent("Request from " + address, request.toString());
                            response = handlePullListRequest(request);
                            Connections.sendFrameRouter(server, address, response);
                            break;
                        case PUSH_LIST:
                            Connections.logEvent("Request from " + address, request.toString());
                            response = handlePushListRequest(request);
                            Connections.sendFrameRouter(server, address, response);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private Frame handlePushListRequest(Frame request) {
        Frame response;
        if (!Connections.doesListExistDB(this.databaseURL, request.getListID())) {
            if(Connections.addListDB(this.databaseURL, request.getListID()))
                Connections.logEvent("List Added", "{List: " + request.getListID() + "}");
        }

        if(Connections.updateListDB(this.databaseURL, request.getListID(), request.getListItem())){
            Connections.logEvent("List Updated", "{List: " + request.getListID() + "}");
            return response = new Frame(Frame.FrameStatus.SERVER_OK, Frame.FrameAction.PUSH_LIST, request.getListID(), "");
        }

        return null;
    }

    private Frame handlePullListRequest(Frame request) {
        Frame response;
        if (!Connections.doesListExistDB(this.databaseURL, request.getListID())) {
            Connections.logEvent("List not Found", "{List: " + request.getListID() + "}");
            return response= new Frame(Frame.FrameStatus.SERVER_ERROR, Frame.FrameAction.PULL_LIST, request.getListID(), "");
        }

        String items = Connections.getItemsDB(this.databaseURL, request.getListID());
        if (items != null) {
            Connections.logEvent("List Pulled", "{List: " + request.getListID() + "}");
            return response = new Frame(Frame.FrameStatus.SERVER_OK, Frame.FrameAction.PULL_LIST, request.getListID(), items);
        }

        return null;
    }

    private boolean connectOrCreateDatabase() {
        Connection connection;
        this.databaseURL = "jdbc:sqlite:./src/main/java/shoppingList/server/server_" + this.id + ".db";

        try {
            // Attempt to connect to the database
            connection = DriverManager.getConnection(this.databaseURL);

            File file = new File("server/server_" + this.id + ".db");
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

        } catch (SQLException e) {
            // Handle table creation errors
            System.err.println("Error creating table: " + e.getMessage());
        }
    }


    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Server <id>");
            System.exit(1);
        }

        Server server = new Server(Integer.parseInt(args[0]));
        server.init();
    }
}
