package shoppingList.server.helper;

public class Item {
    public String name;
    public int quantity;
    public long timestamp; // Timestamp for ordering updates

    public Item(String name, int quantity, long timestamp) {
        this.name = name;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    // Override equals and hashCode for proper comparison and use in data structures
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Item other = (Item) obj;
        return name.equals(other.name) && timestamp == other.timestamp;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "{" +
                "name=" + name +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                "}";
    }
}
