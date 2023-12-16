package shoppingList.client.client_states;

import shoppingList.helper.Connections;
import shoppingList.helper.Utils;

import java.util.ArrayList;
import java.util.Scanner;

public class OpenListsState implements ClientState {
    private final String databaseURL;
    private final String listID;
    private final Scanner scanner = new Scanner(System.in);
    private String items;

    public OpenListsState(String databaseURL, String listID) {
        this.databaseURL = databaseURL;
        this.listID = listID;
    }

    @Override
    public ClientState run() {

        if (!Connections.doesListExistDB(this.databaseURL, this.listID)) {
            System.out.println("List does not exist");
            return new MainMenuState(this.databaseURL);
        }

        //TODO: save items as the hasmap
        this.items = Connections.getItemsDB(this.databaseURL, this.listID);

        while (true) {
            printOpenListMenu();

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    return new AddItemState(this.databaseURL, this.listID);
                case "2":
                    return new RemoveItemState(this.databaseURL, this.listID);
                case "3":
                    //TODO: return new RemoveListState(this.databaseURL, this.listID);
                case "0":
                    return new MainMenuState(this.databaseURL);
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
        System.out.println("[3] Remove List");
        System.out.println("[0] Back");
        System.out.println();
        System.out.print("> ");
    }

    private void printItems() {
        //TODO: print items
        System.out.println("- " + this.items);
        System.out.println();
    }
}
