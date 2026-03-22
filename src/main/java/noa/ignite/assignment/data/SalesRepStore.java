package noa.ignite.assignment.data;

import noa.ignite.assignment.model.SalesRep;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SalesRepStore {
    private final Map<String, SalesRep> emailIndex = new ConcurrentHashMap<>();

    public void add(SalesRep incoming) {
        // Normalize the email key to prevent duplicates due to casing or spaces
        String emailKey = incoming.getEmail().toLowerCase().trim();
        emailIndex.put(emailKey, incoming);
    }

    public void remove(String email) {
        // Ensure consistent lookup by normalizing the email before removal
        emailIndex.remove(email.toLowerCase().trim());
    }

    public boolean exists(String email) {
        if (email == null) return false;
        // Check for presence using the normalized email format
        return emailIndex.containsKey(email.toLowerCase().trim());
    }

    public SalesRep findByEmail(String email) {
        if (email == null || email.isEmpty()) return null;
        // Retrieve the object from the thread safe map
        return emailIndex.get(email.toLowerCase().trim());
    }

    public List<SalesRep> getAll() {
        // Return a new list to protect the internal map from external changes
        return new ArrayList<>(emailIndex.values());
    }
}