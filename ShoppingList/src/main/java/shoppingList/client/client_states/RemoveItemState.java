package shoppingList.client.client_states;

import shoppingList.client.helper.Connections;
import shoppingList.client.helper.Utils;

import java.util.Scanner;

public class RemoveItemState implements ClientState {
    private final String databaseURL;
    private final String listID;
    private final Scanner scanner = new Scanner(System.in);

    public RemoveItemState(String databaseURL, String listID) {
        this.databaseURL = databaseURL;
        this.listID = listID;
    }

    @Override
    public ClientState run() {

        System.out.print("Item Name: ");
        String itemName = scanner.nextLine();

        if (!Connections.doesItemExistDB(this.databaseURL, this.listID, itemName)) {
            System.out.println("Item does not exist");
            return new OpenListsState(this.databaseURL, this.listID);
        }

        if (!Connections.removeItemDB(this.databaseURL, this.listID, itemName)) {
            System.out.println("Failed to remove item");
        }

        return new OpenListsState(this.databaseURL, this.listID);
    }

    private void printRemoveItemMenu() {
        Utils.clearTerminal();
        System.out.println("===========");
        System.out.println("Remove Item");
        System.out.println("===========");
        System.out.println();
    }
}
