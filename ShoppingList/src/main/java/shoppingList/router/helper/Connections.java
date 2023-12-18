package shoppingList.router.helper;

import com.google.gson.Gson;
import org.zeromq.ZMQ;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Connections {
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public static boolean sendFrameFrontend(ZMQ.Socket socket, String clientAddress, Frame frame) {
        return socket.sendMore(clientAddress) && socket.sendMore("") && socket.send(new Gson().toJson(frame));
    }

    public static boolean sendFrameBackend(ZMQ.Socket socket, String serverAddress, String clientAddress, Frame frame) {
        return socket.sendMore(serverAddress) && socket.sendMore("") && socket.sendMore(clientAddress) && socket.sendMore("") && socket.send(new Gson().toJson(frame));
    }

    public static void logEvent(String event, String details) {
        String timestamp = TIMESTAMP_FORMAT.format(new Date());
        System.out.printf("[%-8s]   %-37s %s%n", timestamp, event, details);
    }

}
