package shoppingList.client.helper;

public class Frame {
    private final FrameStatus status;
    private final FrameAction action;
    private final String data;

    public Frame (FrameStatus status, FrameAction action, String data) {
        this.status = status;
        this.action = action;
        this.data = data;
    }

    public FrameStatus getStatus() {
        return status;
    }

    public FrameAction getAction() {
        return action;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("{%s?%s?%s}", status, action, data);
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

