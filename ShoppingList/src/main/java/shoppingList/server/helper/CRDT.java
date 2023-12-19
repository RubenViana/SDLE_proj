package shoppingList.server.helper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import shoppingList.server.helper.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CRDT {
    private Map<String, Item> items = new HashMap<>();
    public CRDT(String itemsList) {
        JsonArray jsonArray = new Gson().fromJson(itemsList, JsonArray.class);
        for (JsonElement item : jsonArray) {
            Item newItem = new Gson().fromJson(item, Item.class);
            items.put(newItem.name, newItem);
        }
    }

    public void addOrUpdateItem(Item newItem) {
        items.put(newItem.name, newItem);
    }

    // Removing an Item is equivalent to setting its quantity to 0
    public void removeItem(Item removedItem) {
        Item newItem = new Item(removedItem.name, 0, removedItem.timestamp);
        items.put(newItem.name, newItem);
    }

    // Method to merge two lists based on timestamps
    public void merge(String otherItemList) {
        JsonArray otherJsonArray = new Gson().fromJson(otherItemList, JsonArray.class);
        for (JsonElement otherItem : otherJsonArray) {
            Item item = new Gson().fromJson(otherItem, Item.class);
            items.merge(item.name, item, (existingItem, newItem) ->
                    (existingItem.timestamp >= newItem.timestamp) ? existingItem : newItem
            );
        }
    }

    // Method to get the current state as a JSON string
    public String getItemsList() {
        return new Gson().toJson(items.values());
    }

    public Collection<Item> getItems() {
        return items.values();
    }

    public Item getItem(String name) {
        return items.get(name);
    }
}

