package shoppingList.router.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;

public class Frame implements java.io.Serializable {
    private final FrameStatus status;
    private final FrameAction action;
    private final String data;
    private final String timestamp;

    public Frame (FrameStatus status, FrameAction action, String data) {
        this.status = status;
        this.action = action;
        this.data = data;
        this.timestamp = Instant.now().toString();
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

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s", status, action, data, timestamp);
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

