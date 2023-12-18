package shoppingList.client.client_states;

import shoppingList.client.helper.Connections;
import shoppingList.client.helper.Utils;
import java.util.UUID;

public class CreateListState implements ClientState {
    private final String databaseURL;
    private final String userID;
    private String listID;
    public CreateListState(String databaseURL, String userID) {
        this.databaseURL = databaseURL;
        this.userID = userID;
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


        return new OpenListsState(this.databaseURL, this.userID, this.listID);
    }

    private void printCreateListMenu() {
        Utils.clearTerminal();
        System.out.println("==========================================================");
        System.out.println("List created with ID: " + this.listID);
        System.out.println("==========================================================");
    }
}
