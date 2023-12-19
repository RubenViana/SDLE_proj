package shoppingList.client.client_states;

import shoppingList.client.helper.CRDT;
import shoppingList.client.helper.Connections;
import shoppingList.client.helper.Item;
import shoppingList.client.helper.Utils;

import java.util.Scanner;

public class OpenListsState implements ClientState {
    private final String databaseURL;
    private final String userID;
    private final String listID;
    private final Scanner scanner = new Scanner(System.in);
    private String items;

    public OpenListsState(String databaseURL, String userID, String listID) {
        this.databaseURL = databaseURL;
        this.listID = listID;
        this.userID = userID;
    }

    @Override
    public ClientState run() {

        /*if (!Connections.pullListFromServer(this.databaseURL, this.listID)) {
            System.out.println("Error pulling list from server");
        }*/

        if (!Connections.doesListExistDB(this.databaseURL, this.listID)) {
            System.out.println("List does not exist locally");
            if (!Connections.pullListFromServer(this.databaseURL, this.userID, this.listID)) {
                System.out.println("List does not exist in server");
                return new MainMenuState(this.databaseURL, this.userID);
            }
            return new OpenListsState(this.databaseURL, this.userID, this.listID);
        }

        //TODO: save items as the hashmap
        this.items = Connections.getItemsDB(this.databaseURL, this.listID);

        while (true) {
            printOpenListMenu();

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    return new AddItemState(this.databaseURL, this.userID, this.listID);
                case "2":
                    return new RemoveItemState(this.databaseURL, this.userID, this.listID);
                case "3":
                    return new UpdateItemState(this.databaseURL, this.userID, this.listID);
                case "0":
                    return new MainMenuState(this.databaseURL, this.userID);
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void printOpenListMenu() {
        Utils.clearTerminal();
        System.out.println("=========================================");
        System.out.println("List " + this.listID);
        System.out.println("=========================================");
        printItems();
        System.out.println("[1] Add Item");
        System.out.println("[2] Remove Item");
        System.out.println("[3] Update Item");
        System.out.println("[0] Back");
        System.out.println();
        System.out.print("> ");
    }

    private void printItems() {
        CRDT crdt = new CRDT(this.items);
        for (Item item : crdt.getItems()) {
            System.out.println("- " + item.name + ": " + item.quantity);
        }
        System.out.println();
    }
}
