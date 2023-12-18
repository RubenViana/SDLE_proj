package shoppingList.client.client_states;

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
        //TODO: remove list from server

        return new MainMenuState(this.databaseURL, this.userID);
    }
}
