package shoppingList.client.helper;

public class Frame {
    private final FrameStatus status;
    private final FrameAction action;
    private final String listID;
    private final String listItem;

    public Frame (FrameStatus status, FrameAction action, String listID, String listItem) {
        this.status = status;
        this.action = action;
        this.listID = listID;
        this.listItem = listItem;
    }

    public FrameStatus getStatus() {
        return status;
    }

    public FrameAction getAction() {
        return action;
    }

    public String getListID() {
        return listID;
    }

    public String getListItem() {
        return listItem;
    }

    @Override
    public String toString() {
        return String.format("{%s?%s?%s?%s}", status, action, listID, listItem);
    }


    public enum FrameStatus {
        ROUTER_OK,
        ROUTER_ERROR,
        SERVER_OK,
        SERVER_ERROR,
        CLIENT_OK,
    }

    public enum FrameAction {
        ROUTER_STATUS,
        SERVER_STATUS,
        ADD_LIST,
        REMOVE_LIST,
        PULL_LIST,
        PUSH_LIST,
    }
}

