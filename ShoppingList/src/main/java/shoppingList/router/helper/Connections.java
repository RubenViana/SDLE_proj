package shoppingList.router.helper;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
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

    public static boolean checkServerStatus(Integer serverID, String serverAddress) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://localhost:" + serverAddress);

            socket.setReceiveTimeOut(1000);

            Connections.logEvent( "Checking Server_" + serverID + " Status", "{Server: " + serverAddress + "}");

            Frame request = new Frame(Frame.FrameStatus.SERVER_OK, Frame.FrameAction.SERVER_STATUS, "", "");

            socket.send(new Gson().toJson(request));

            Frame response = new Gson().fromJson(socket.recvStr(), Frame.class);

            if (response == null) {
                Connections.logEvent( "No response from Server_" + serverID, "{Server: " + serverAddress + "}");
                socket.close();
                return false;
            }

            Connections.logEvent( "Server_" + serverID + " Online", "{Server: " + serverAddress + "}");
            socket.close();
            return true;
        }
    }

}
