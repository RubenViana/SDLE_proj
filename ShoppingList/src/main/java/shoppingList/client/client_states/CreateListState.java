package shoppingList.client.client_states;

import shoppingList.helper.Connections;
import shoppingList.helper.Utils;
import java.util.UUID;

public class CreateListState implements ClientState {
    private final String databaseURL;
    private String listID;
    public CreateListState(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    @Override
    public ClientState run() {

        System.out.println("Create List State");

        UUID listID = UUID.randomUUID();
        this.listID = listID.toString();

        if (!Connections.addListDB(this.databaseURL, this.listID)) {
            System.out.println("Error creating list");
            return null;
        }

        printCreateListMenu();


        return new OpenListsState(this.databaseURL, this.listID);
    }

    private void printCreateListMenu() {
        Utils.clearTerminal();
        System.out.println("==========================================================");
        System.out.println("List created with ID: " + this.listID);
        System.out.println("==========================================================");
    }
}
