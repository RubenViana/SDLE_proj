package shoppingList.client.client_states;

import shoppingList.helper.Connections;
import shoppingList.helper.Utils;

import java.util.ArrayList;
import java.util.Scanner;

public class ListListsState implements ClientState {
    private final String databaseURL;
    private final Scanner scanner = new Scanner(System.in);
    private ArrayList<String> lists = new ArrayList<>();

    public ListListsState(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    @Override
    public ClientState run() {

        this.lists = Connections.getListsDB(this.databaseURL);

        while (true) {
            printListListsMenu();

            String option = scanner.nextLine();
            String listID = null;
            String[] opts = option.split("\\s+");
            if (opts.length == 2 && opts[0].equals("1")) {
                listID = opts[1];
            }
            if (listID == null && opts.length == 2) {
                System.out.println("Invalid option");
                continue;
            }

            switch (opts[0]) {
                case "1":
                    return new OpenListsState(this.databaseURL, listID);
                case "0":
                    return new MainMenuState(this.databaseURL);
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void printListListsMenu() {
        Utils.clearTerminal();
        System.out.println("===========");
        System.out.println("Local Lists");
        System.out.println("===========");
        System.out.println();
        printLists();
        System.out.println("[1] - Open List <listID>");
        System.out.println("[0] - Back");
        System.out.println();
        System.out.print("> ");
    }

    private void printLists() {
        for (String list : this.lists) {
            System.out.println("ListID: " + list);
        }
        System.out.println();
    }
}
