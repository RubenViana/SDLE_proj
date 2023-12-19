package shoppingList.server.server_states;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import shoppingList.client.client_states.ClientState;
import shoppingList.server.Server;
import shoppingList.server.helper.Connections;
import shoppingList.server.helper.Frame;

import java.util.concurrent.*;

public class OnlineState implements ServerState {
    private final int serverID;
    private final String databaseURL;
    private final ZMQ.Socket routerSocket;
    private long lastRouterRequestTime;

    public OnlineState(int serverID, String databaseURL, ZMQ.Socket routerSocket) {
        this.serverID = serverID;
        this.databaseURL = databaseURL;
        this.routerSocket = routerSocket;
    }

    @Override
    public ServerState run() {

        try (ZContext context = new ZContext()) {
            ZMQ.Socket server = context.createSocket(SocketType.REP);
            int serverPort = Connections.SERVER_PORT + this.serverID;
            server.bind("tcp://*:" + serverPort);

            // Create a ScheduledExecutorService with a single thread
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            // not very elegant, but it works
            scheduler.scheduleAtFixedRate(() -> {if (System.currentTimeMillis() - lastRouterRequestTime > 10000) {
                Connections.logEvent("Timeout from Router", "{Router: }");
                scheduler.shutdown();
                ServerState newState = new ConnectRouterState(serverID, databaseURL);
                newState.run();
            }}, 10, 30, TimeUnit.SECONDS);



            while (!Thread.currentThread().isInterrupted()) {
                Frame request = new Gson().fromJson(server.recvStr(), Frame.class);
                Frame response;
                lastRouterRequestTime = System.currentTimeMillis();

                switch (request.getAction()) {
                    case SERVER_STATUS:
                        Connections.logEvent("Reply to Router", "{Router: }");
                        response = new Frame(Frame.FrameStatus.SERVER_OK, Frame.FrameAction.SERVER_STATUS, "", "");
                        server.send(new Gson().toJson(response));
                        break;
                    case PULL_LIST:
                        Connections.logEvent("Request from Client", request.toString());
                        response = handlePullListRequest(request);
                        server.send(new Gson().toJson(response));
                        break;
                    case PUSH_LIST:
                        Connections.logEvent("Request from Client", request.toString());
                        response = handlePushListRequest(request);
                        server.send(new Gson().toJson(response));
                        break;
                    default:
                        break;
                }
            }
        }
        return null;
    }

    private Frame handlePushListRequest(Frame request) {
        Frame response;
        if (!Connections.doesListExistDB(this.databaseURL, request.getListID())) {
            if(Connections.addListDB(this.databaseURL, request.getListID(), request.getListItem()))
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
}
