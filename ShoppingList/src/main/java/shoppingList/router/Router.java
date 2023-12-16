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


            for (int workerNbr = 0; workerNbr < NBR_WORKERS; workerNbr++)
                new WorkerTask().start();


            //  Queue of available workers
            Queue<String> workerQueue = new LinkedList<String>();

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
                    // Server request is [client address][empty][server address][empty][request]
                    String serverAddr = backend.recvStr();
                    String empty = backend.recvStr();
                    assert (empty.isEmpty());
                    String clientAddr = backend.recvStr();
                    empty = backend.recvStr();
                    assert (empty.isEmpty());
                    Frame request = new Gson().fromJson(backend.recvStr(), Frame.class);

                    System.out.println(clientAddr + " - " + request.toString());

                    switch (request.getAction()) {
                        case SERVER_STATUS:
                            System.out.println("SERVER_STATUS");
                            // add server to the list of available servers
                            if (request.getStatus() == Frame.FrameStatus.SERVER_OK)
                                workerQueue.add(serverAddr);
                            //TODO send the future hash ring here to the server
                            break;
                        case PULL_LIST:
                            System.out.println("PULL_LIST");
                            //JUST FOR TESTING
                            Connections.sendFrameFrontend(frontend, clientAddr, new Frame(request.getStatus(), Frame.FrameAction.PULL_LIST, ""));
                            break;
                        case PUSH_LIST:
                            System.out.println("PUSH_LIST");
                            //JUST FOR TESTING
                            Connections.sendFrameFrontend(frontend, clientAddr, new Frame(request.getStatus(), Frame.FrameAction.PUSH_LIST, ""));
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

                    System.out.println(clientAddr + " - " + request.toString());

                    String serverAddr;

                    switch (request.getAction()) {
                        case ROUTER_STATUS:
                            Connections.sendFrameFrontend(frontend, clientAddr, new Frame(Frame.FrameStatus.ROUTER_OK, Frame.FrameAction.ROUTER_STATUS, ""));
                            break;
                        case PULL_LIST:
                            System.out.println("PULL_LIST");
                            serverAddr = workerQueue.poll();//TODO get the correct server address based on the listID
                            System.out.println("serverAddr: " + serverAddr);
                            Connections.sendFrameBackend(backend, serverAddr, clientAddr, new Frame(request.getStatus(), Frame.FrameAction.PULL_LIST, ""));
                            break;
                        case PUSH_LIST:
                            System.out.println("PUSH_LIST");
                            serverAddr = workerQueue.poll();//TODO get the correct server address based on the listID
                            Connections.sendFrameBackend(backend, serverAddr, clientAddr, new Frame(request.getStatus(), Frame.FrameAction.PUSH_LIST, ""));
                            break;
                        default:
                            System.out.println("Invalid action");
                    }

                    //TODO handle replication of requests to other servers
                }
            }
        }
    }



    private static class WorkerTask extends Thread
    {
        @Override
        public void run()
        {
            //  Prepare our context and sockets
            try (ZContext context = new ZContext()) {
                ZMQ.Socket worker = context.createSocket(SocketType.REQ);
                String workerId = Thread.currentThread().getName();
                worker.setIdentity(workerId.getBytes());

                worker.connect("tcp://localhost:" + (5000 + 1000));

                //  Tell backend we're ready for work
                worker.sendMore("SERVER");
                worker.sendMore("");
                worker.send(new Gson().toJson(new Frame(Frame.FrameStatus.SERVER_OK, Frame.FrameAction.SERVER_STATUS, "")));


                while (!Thread.currentThread().isInterrupted()) {
                    String address = worker.recvStr();
                    String empty = worker.recvStr();
                    assert (empty.isEmpty());
                    //  Get request, send reply
                    Frame request = new Gson().fromJson(worker.recvStr(), Frame.class);

                    worker.sendMore(address);
                    worker.sendMore("");
                    worker.send(new Gson().toJson(new Frame(Frame.FrameStatus.SERVER_OK, request.getAction(), "")));
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

