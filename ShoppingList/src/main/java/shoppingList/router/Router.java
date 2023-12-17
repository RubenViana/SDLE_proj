package shoppingList.router;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import shoppingList.router.helper.Connections;
import shoppingList.router.helper.Frame;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Router {
    private final int port;
    private static final int NBR_WORKERS = 3; //ONLY FOR TESTING
    public Router(int port) {
        this.port = port;
    }

    private void init() {

        //TODO handle client requests
        //TODO handle server requests

        try (ZContext context = new ZContext()) {
            ZMQ.Socket frontend = context.createSocket(SocketType.ROUTER);
            ZMQ.Socket backend = context.createSocket(SocketType.ROUTER);
            frontend.bind("tcp://*:" + port);
            backend.bind("tcp://*:" + (port + 1000));
            Connections.logEvent( "Router Online", "{Frontend: " + port + " | Backend: " + (port + 1000) + "}");


            //  Queue of available workers
            ArrayList<String> servers = new ArrayList<>();

            while (!Thread.currentThread().isInterrupted()) {
                //  Initialize poll set
                ZMQ.Poller items = context.createPoller(2);

                //Always poll for server and client activity
                items.register(backend, ZMQ.Poller.POLLIN);
                items.register(frontend, ZMQ.Poller.POLLIN);

                if (items.poll() < 0) {
                    Connections.logEvent( "Router Interrupted", "{}");
                    break;  //  Interrupted
                }

                //  Handle server on backend
                if (items.pollin(0)) {
                    // Server request is [client address][empty][server address][empty][request]
                    String serverAddr = backend.recvStr();
                    String empty = backend.recvStr();
                    assert (empty.isEmpty());
                    String clientAddr = backend.recvStr();
                    empty = backend.recvStr();
                    assert (empty.isEmpty());
                    Frame request = new Gson().fromJson(backend.recvStr(), Frame.class);

                    switch (request.getAction()) {
                        case SERVER_STATUS:
                            // add server to the list of available servers
                            if (request.getStatus() == Frame.FrameStatus.SERVER_OK)
                                servers.add(serverAddr);
                            //TODO send the future hash ring here to the server
                            Connections.logEvent(serverAddr + " Online", request.toString());
                            break;
                        case PULL_LIST:
                            //JUST FOR TESTING
                            Connections.sendFrameFrontend(frontend, clientAddr, request);
                            Connections.logEvent( "Response from " + serverAddr + " to " + clientAddr, request.toString());
                            break;
                        case PUSH_LIST:
                            //JUST FOR TESTING
                            Connections.sendFrameFrontend(frontend, clientAddr, request);
                            Connections.logEvent( "Response from " + serverAddr + " to " + clientAddr, request.toString());
                            break;
                        default:
                            System.out.println("Invalid action");
                    }
                }

                if (items.pollin(1)) {
                    // Client request is [client address][empty][request]
                    String clientAddr = frontend.recvStr();
                    String empty = frontend.recvStr();
                    assert (empty.isEmpty());
                    Frame request = new Gson().fromJson(frontend.recvStr(), Frame.class);

                    String serverAddr;

                    switch (request.getAction()) {
                        case ROUTER_STATUS:
                            Connections.sendFrameFrontend(frontend, clientAddr, new Frame(Frame.FrameStatus.ROUTER_OK, Frame.FrameAction.ROUTER_STATUS, "", ""));
                            Connections.logEvent( "Reply to " + clientAddr, request.toString());
                            break;
                        case PULL_LIST:
                            serverAddr = servers.get(0);//TODO get the correct server address based on the listID
                            Connections.sendFrameBackend(backend, serverAddr, clientAddr, request);
                            Connections.logEvent( "Request from " + clientAddr + " to " + serverAddr, request.toString());
                            break;
                        case PUSH_LIST:
                            serverAddr = servers.get(0);//TODO get the correct server address based on the listID
                            Connections.sendFrameBackend(backend, serverAddr, clientAddr, request);
                            Connections.logEvent( "Request from " + clientAddr + " to " + serverAddr, request.toString());
                            break;
                        default:
                            System.out.println("Invalid action");
                    }

                    //TODO handle replication of requests to other servers
                }
            }
        }
    }


    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Router <port>");
            System.exit(1);
        }

        Router router = new Router(Integer.parseInt(args[0]));

        router.init();
    }
}

