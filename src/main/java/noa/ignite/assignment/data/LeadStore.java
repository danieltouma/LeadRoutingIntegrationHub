package noa.ignite.assignment.data;

import noa.ignite.assignment.model.Lead;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeadStore {
    private final Map<String, Lead> emailIndex = new ConcurrentHashMap<>();
    private final Map<String, Lead> compositeIndex = new ConcurrentHashMap<>();

    public void save(Lead lead) {
        // Create unique identifiers
        String emailKey = lead.getEmail().toLowerCase().trim();
        String compositeKey = createCompositeKey(lead);

        // Store lead with two unique identifiers
        emailIndex.put(emailKey, lead);
        compositeIndex.put(compositeKey, lead);
    }

    public void update(Lead existing, Lead incoming) {
        // Remove existing identifiers
        String oldEmailKey = existing.getEmail().toLowerCase().trim();
        String oldCompositeKey = createCompositeKey(existing);
        emailIndex.remove(oldEmailKey);
        compositeIndex.remove(oldCompositeKey);

        // Rewrite existing lead with new lead info
        existing.setFirstName(incoming.getFirstName());
        existing.setLastName(incoming.getLastName());
        existing.setEmail(incoming.getEmail());
        existing.setCompany(incoming.getCompany());
        existing.setTerritory(incoming.getTerritory());
        existing.setSource(incoming.getSource());
        // Save to LeadStore
        save(existing);
    }

    public Lead findLead(Lead incoming) {
        // Find match based on email
        String emailKey = incoming.getEmail().toLowerCase().trim();
        if (emailIndex.containsKey(emailKey)) {
            System.out.println("Duplicate found");
            return emailIndex.get(emailKey);
        }

        // Find match based on lastName, firstName and company
        String compositeKey = createCompositeKey(incoming);
        if (compositeIndex.containsKey(compositeKey)) {
            System.out.println("Duplicate found");
            return compositeIndex.get(compositeKey);
        }
        // Return null if no match found
        return null;
    }

    private String createCompositeKey(Lead lead) {
        // Create unique identifier based on exact match of firstName, lastName and company
        return (lead.getFirstName().trim() + "|" +
                lead.getLastName().trim() + "|" +
                lead.getCompany().trim()).toLowerCase();
    }

    public List<Lead> getAll() {
        return new ArrayList<>(emailIndex.values());
    }
}