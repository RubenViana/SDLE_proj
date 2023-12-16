package shoppingList.router;

import com.google.gson.Gson;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import shoppingList.router.helper.Connections;
import shoppingList.router.helper.Frame;

import java.util.LinkedList;
import java.util.Queue;

public class Router {
    private final int port;
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


            while (!Thread.currentThread().isInterrupted()) {
                //  Initialize poll set
                ZMQ.Poller items = context.createPoller(2);

                //Always poll for server and client activity
                items.register(backend, ZMQ.Poller.POLLIN);
                items.register(frontend, ZMQ.Poller.POLLIN);

                if (items.poll() < 0) {
                    System.out.println("Router interrupted");
                    break;  //  Interrupted
                }

                //  Handle server on backend
                if (items.pollin(0)) {
                    System.out.println("SERVER REQUEST");

                    /*//  Queue worker address for LRU routing
                    workerQueue.add(backend.recvStr());

                    //  Second frame is empty
                    String empty = backend.recvStr();
                    assert (empty.isEmpty());

                    //  Third frame is READY or else a client reply address
                    String clientAddr = backend.recvStr();

                    //  If client reply, send rest back to frontend
                    if (!clientAddr.equals("READY")) {

                        empty = backend.recvStr();
                        assert (empty.isEmpty());

                        String reply = backend.recvStr();
                        frontend.sendMore(clientAddr);
                        frontend.sendMore("");
                        frontend.send(reply);

                    }*/
                }

                if (items.pollin(1)) {
                    //  Client request is [address][empty][request]
                    String clientAddr = frontend.recvStr();
                    String empty = frontend.recvStr();
                    assert (empty.isEmpty());
                    Frame request = new Gson().fromJson(frontend.recvStr(), Frame.class);

                    System.out.println(clientAddr + " - " + request.toString());

                    switch (request.getAction()) {
                        case ROUTER_STATUS:
                            Connections.sendFrame(frontend, clientAddr, new Frame(Frame.FrameStatus.ROUTER_OK, Frame.FrameAction.ROUTER_STATUS, ""));
                            break;
                        case SERVER_STATUS:
                            System.out.println("SERVER_STATUS");
                            break;
                        case PULL_LIST:
                            System.out.println("PULL_LIST");
                            //JUST FOR TESTING
                            Connections.sendFrame(frontend, clientAddr, new Frame(Frame.FrameStatus.SERVER_OK, Frame.FrameAction.PULL_LIST, ""));
                            break;
                        case PUSH_LIST:
                            System.out.println("PUSH_LIST");
                            break;
                        case ADD_LIST:
                            System.out.println("ADD_ITEM");
                            break;
                        case REMOVE_LIST:
                            System.out.println("DELETE_LIST");
                            break;
                        default:
                            System.out.println("Invalid action");
                    }

                    //TODO get the correct server address based on the listID
                    /*String workerAddr = workerQueue.poll();

                    backend.sendMore(workerAddr);
                    backend.sendMore("");
                    backend.sendMore(clientAddr);
                    backend.sendMore("");
                    backend.send(request);*/

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

        System.out.println("Router '" + args[0] + "' Started");
        Router router = new Router(Integer.parseInt(args[0]));

        router.init();
    }
}

