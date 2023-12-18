package shoppingList.server;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import shoppingList.server.helper.Connections;
import shoppingList.server.helper.Frame;
import shoppingList.server.server_states.InitState;
import shoppingList.server.server_states.ServerState;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    private final int id;
    private String databaseURL;


    public Server(int id) {
        this.id = id;
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Server <id>");
            System.exit(1);
        }

        ServerState state = new InitState(Integer.parseInt(args[0]));

        while (state != null) {
            state = state.run();
        }
    }
}
