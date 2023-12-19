package shoppingList.router.helper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashing {
    private final int numberOfReplicas; // Number of virtual nodes per server
    private final SortedMap<String, Integer> hashRing = new TreeMap<>(); // Hash ring
    private final MessageDigest hashFunction;

    public ConsistentHashing(int numberOfReplicas) {

        try {
            this.hashFunction = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }

        this.numberOfReplicas = numberOfReplicas;

    }

    public void addServer(Integer server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            String key = "Server" + server + "_Node" + i;
            String hash = hash(key);
            hashRing.put(hash, server);
        }
    }

    public void removeServer(Integer server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            String key = "Server" + server + "_Node" + i;
            String hash = hash(key);
            hashRing.remove(hash);
        }
    }

    private String hash(String key) {
        hashFunction.reset();
        byte[] bytes = hashFunction.digest(key.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(bytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public Integer getServer(String key) {
        if (hashRing.isEmpty()) {
            return null;
        }
        String hash = hash(key);
        if (!hashRing.containsKey(hash)) {
            SortedMap<String, Integer> tailMap = hashRing.tailMap(hash);
            hash = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
        }
        return hashRing.get(hash);
    }
}
