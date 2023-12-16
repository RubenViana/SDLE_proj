package shoppingList.client.client_states;

public class RemoveListState implements ClientState{
    private final String databaseURL;
    private final String listID;

    public RemoveListState(String databaseURL, String listID) {
        this.databaseURL = databaseURL;
        this.listID = listID;
    }

    @Override
    public ClientState run() {
        return null;
    }
}
