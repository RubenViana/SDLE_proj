package shoppingList.server.server_states;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import shoppingList.server.helper.Connections;
import shoppingList.server.helper.Frame;

public class ConnectRouterState implements ServerState {
    private final int serverID;
    private final String databaseURL;

    public ConnectRouterState(int serverID, String databaseURL) {

        this.serverID = serverID;
        this.databaseURL = databaseURL;
    }

    @Override
    public ServerState run() {
        for (int port : Connections.routersPorts) {
            try (ZContext context = new ZContext()) {
                ZMQ.Socket socket = context.createSocket(SocketType.REQ);
                Integer serverPort = Connections.SERVER_PORT + this.serverID;
                socket.setIdentity((this.serverID + "").getBytes());
                socket.connect("tcp://localhost:" + port);
                //Send server status message to router, to later enter the ring
                socket.send(new Gson().toJson(new Frame(Frame.FrameStatus.SERVER_OK, Frame.FrameAction.SERVER_STATUS, serverPort + "", "")));

                socket.setReceiveTimeOut(1000);
                Frame response = new Gson().fromJson(socket.recvStr(), Frame.class);

                if (response != null && response.getStatus() == Frame.FrameStatus.ROUTER_OK) {
                    Connections.logEvent("Connected to router", "{Router: " + port + "}");
                    return new OnlineState(this.serverID, this.databaseURL, socket);
                } else {
                    Connections.logEvent("Failed to connect to router", "{Router: " + port + "}");
                    socket.close();
                }
            }
        }
        Connections.logEvent("No router available", "{Router: }");
        return null;
    }
}
