package shoppingList.router.helper;

import com.google.gson.Gson;
import org.zeromq.ZMQ;

public class Connections {

    public static boolean sendFrame(ZMQ.Socket socket, String clientAddress, Frame frame) {
        return socket.sendMore(clientAddress) && socket.sendMore("") && socket.send(new Gson().toJson(frame));
    }
}
