package shoppingList.router;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import shoppingList.router.helper.Connections;
import shoppingList.router.helper.Frame;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Router {
    private final int port;
    private ArrayList<String> servers = new ArrayList<>();
    private Map<String, Long> lastStatusTimeServersMap = new HashMap<>();
    private final ReadWriteLock serversLock = new ReentrantReadWriteLock();
    private final long STATUS_TIMEOUT = 10000;
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
            backend.bind("tcp://*:" + (port + 1000));//Maybe not needed
            Connections.logEvent( "Router Online", "{Frontend: " + port + " | Backend: " + (port + 1000) + "}");

            /*// Create a ScheduledExecutorService with a single thread
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            // send server status to router every 5 seconds
            scheduler.scheduleAtFixedRate(() -> {if (!servers.isEmpty()) {handleTimeoutServers();}}, 0, 1, TimeUnit.SECONDS);*/

            while (!Thread.currentThread().isInterrupted()) {
                //  Initialize poll set
                ZMQ.Poller items = context.createPoller(1);

                items.register(frontend, ZMQ.Poller.POLLIN);

                if (items.poll() < 0) {
                    Connections.logEvent( "Router Interrupted", "{}");
                    break;  //  Interrupted
                }

                //handleTimeoutServers();

                if (items.pollin(0)) {
                    // Client request is [client address][empty][request]
                    String address = frontend.recvStr();
                    String empty = frontend.recvStr();
                    assert (empty.isEmpty());
                    Frame request = new Gson().fromJson(frontend.recvStr(), Frame.class);

                    switch (request.getAction()) {
                        case SERVER_STATUS:
                            handleServerStatus(frontend, address, request);
                            break;
                        case ROUTER_STATUS:
                            Connections.sendFrameFrontend(frontend, address, new Frame(Frame.FrameStatus.ROUTER_OK, Frame.FrameAction.ROUTER_STATUS, "", ""));
                            Connections.logEvent( "Reply to " + address, request.toString());
                            break;
                        case PULL_LIST, PUSH_LIST:
                            handlePullPushListRequest(frontend, address, request);
                            break;
                        default:
                            System.out.println("Invalid action");
                    }
                }
            }
        }
    }

    private void handlePullPushListRequest(ZMQ.Socket router, String client, Frame request) {
        //TESTING ONLY
        if (servers.isEmpty())
            return;

        String serverAddr = servers.get(0);//TODO: change this to a hash ring

        //TODO handle replication of requests to other servers

        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://localhost:" + serverAddr);

            socket.setReceiveTimeOut(1000);

            Connections.logEvent( "Request from " + client + " to " + serverAddr, request.toString());

            socket.send(new Gson().toJson(request));

            Frame response = new Gson().fromJson(socket.recvStr(), Frame.class);

            if (response == null) {
                Connections.logEvent( "No response from server " + serverAddr, request.toString());
                Connections.sendFrameFrontend(router, client, new Frame(Frame.FrameStatus.SERVER_ERROR, request.getAction(), request.getListID(), request.getListItem()));
                socket.close();
                return;
            }

            Connections.logEvent( "Response from " + serverAddr + " to " + client, response.toString());
            Connections.sendFrameFrontend(router, client, response);
            socket.close();

        }
    }

    private void handleServerStatus (ZMQ.Socket router, String server, Frame request) {
        if (request.getStatus() == Frame.FrameStatus.SERVER_OK) {
            if (!servers.contains(server)) {
                servers.add(server);
            }
            lastStatusTimeServersMap.put(server, System.currentTimeMillis());
        }
        // TODO: Send the future hash ring here to the server
        Connections.logEvent("Server" + server + " Online", "{Server: " + server + "}");
        Frame response = new Frame(Frame.FrameStatus.ROUTER_OK, Frame.FrameAction.SERVER_STATUS, "", "");
        Connections.sendFrameFrontend(router, server, response);
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

