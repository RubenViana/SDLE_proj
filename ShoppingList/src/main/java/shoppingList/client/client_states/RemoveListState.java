package shoppingList.client.client_states;

import shoppingList.client.helper.Connections;

public class RemoveListState implements ClientState{
    private final String databaseURL;
    private final String listID;
    private final String userID;

    public RemoveListState(String databaseURL, String userID, String listID) {
        this.databaseURL = databaseURL;
        this.listID = listID;
        this.userID = userID;
    }

    @Override
    public ClientState run() {

        if (!Connections.removeListDB(this.databaseURL, this.listID)) {
            System.out.println("Error removing list");
        }

        return new MainMenuState(this.databaseURL, this.userID);
    }
}
