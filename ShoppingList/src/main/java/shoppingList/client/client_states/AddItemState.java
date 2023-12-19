package shoppingList.client.client_states;

import shoppingList.client.helper.Connections;
import shoppingList.client.helper.Utils;

import java.util.Scanner;

public class AddItemState implements ClientState {
    private final String databaseURL;
    private final String userID;
    private final String listID;
    private final Scanner scanner = new Scanner(System.in);

    public AddItemState(String databaseURL, String userID, String listID) {
        this.databaseURL = databaseURL;
        this.listID = listID;
        this.userID = userID;
    }

    @Override
    public ClientState run() {

        System.out.print("Item Name: ");
        String itemName = scanner.nextLine();
        System.out.print("Item Quantity: ");
        String itemQuantity = scanner.nextLine();

        //TODO: add itemQuantity to addItemDB and check if quantity is valid

        if (Connections.doesItemExistDB(this.databaseURL, this.listID, itemName)) {
            System.out.println("Item already exists");
            return new OpenListsState(this.databaseURL, this.userID, this.listID);
        }

        if (!Connections.addItemDB(this.databaseURL, this.userID, this.listID, itemName, Integer.parseInt(itemQuantity))) { //missing itemQuantity
            System.out.println("Failed to add item");
        }

        return new OpenListsState(this.databaseURL, this.userID, this.listID);
    }

    private void display() {
        Utils.clearTerminal();
        System.out.println("========");
        System.out.println("Add Item");
        System.out.println("========");
        System.out.println();
    }
}
