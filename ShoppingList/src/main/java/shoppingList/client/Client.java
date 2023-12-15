package shoppingList.client;

import shoppingList.client.client_states.ClientState;
import shoppingList.client.client_states.InitState;

import static java.lang.System.console;
import static java.lang.System.exit;

public class Client {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Client <userID>");
            exit(1);
        }
        System.out.println("Client Started");
        ClientState state = new InitState(args[0]);

        while (state != null) {
            state = state.run();
        }

    }
}