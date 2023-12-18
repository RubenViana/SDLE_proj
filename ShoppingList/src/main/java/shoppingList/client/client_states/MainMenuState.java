package shoppingList.client.client_states;

import shoppingList.client.helper.Connections;
import shoppingList.client.helper.Utils;
import java.util.Scanner;

public class MainMenuState implements ClientState {
    private final String databaseURL;
    private final String userID;
    private final Scanner scanner = new Scanner(System.in);
    public MainMenuState(String databaseURL, String userID) {
        this.databaseURL = databaseURL;
        this.userID = userID;
    }

    @Override
    public ClientState run() {
        while (true) {
            printMainMenu();

            String option = scanner.nextLine();
            String listID = null;
            String[] opts = option.split("\\s+");
            if (opts.length == 2 && opts[0].equals("2")) {
                listID = opts[1];
            }
            if (listID == null && opts.length == 2) {
                System.out.println("Invalid option");
                continue;
            }

            switch (opts[0]) {
                case "1":
                    return new CreateListState(this.databaseURL, this.userID);
                case "2":
                    return new OpenListsState(this.databaseURL, this.userID, listID);
                case "3":
                    return new ListListsState(this.databaseURL, this.userID);
                case "0":
                    return null;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void printMainMenu() {
        Utils.clearTerminal();
        System.out.println("=========");
        System.out.println("Main Menu");
        System.out.println("=========");
        System.out.println();
        System.out.println("[1] Create List");
        System.out.println("[2] Open List <listID>");
        System.out.println("[3] List Lists");
        System.out.println("[0] Exit");
        System.out.println();
        System.out.print("> ");
    }
}
