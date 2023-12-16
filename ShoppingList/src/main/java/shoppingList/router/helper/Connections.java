package shoppingList.router.helper;

import com.google.gson.Gson;
import org.zeromq.ZMQ;

public class Connections {

    public static boolean sendFrameFrontend(ZMQ.Socket socket, String clientAddress, Frame frame) {
        return socket.sendMore(clientAddress) && socket.sendMore("") && socket.send(new Gson().toJson(frame));
    }

    public static boolean sendFrameBackend(ZMQ.Socket socket, String serverAddress, String clientAddress, Frame frame) {
        return socket.sendMore(serverAddress) && socket.sendMore("") && socket.sendMore(clientAddress) && socket.sendMore("") && socket.send(new Gson().toJson(frame));
    }
}
