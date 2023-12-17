package shoppingList.server.helper;

public class Frame {
    private final Frame.FrameStatus status;
    private final Frame.FrameAction action;
    private final String listID;
    private final String listItem;

    public Frame (Frame.FrameStatus status, Frame.FrameAction action, String listID, String listItem) {
        this.status = status;
        this.action = action;
        this.listID = listID;
        this.listItem = listItem;
    }

    public Frame.FrameStatus getStatus() {
        return status;
    }

    public Frame.FrameAction getAction() {
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


