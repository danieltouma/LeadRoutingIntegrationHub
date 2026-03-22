package noa.ignite.assignment.ingestion;

import noa.ignite.assignment.model.*;
import noa.ignite.assignment.data.LeadStore;
import noa.ignite.assignment.util.ValidationUtils;

import java.util.ArrayList;
import java.util.List;

public class LeadProcessor {
    private final LeadStore leadDb;

    public LeadProcessor(LeadStore leadDb) {
        this.leadDb = leadDb;
    }

    public synchronized void processIncomingLead(Lead incoming) {
        // Check if lead info is valid
        if (!ValidationUtils.isValidLead(incoming)) {
            System.out.println("Lead data is incomplete.");
            return;
        }

        // Check if lead already exists
        Lead existing = leadDb.findLead(incoming);

        // Update if lead exists otherwise save new
        if (existing != null) {
            updateLead(existing, incoming);
        } else {
            leadDb.save(incoming);
            System.out.println("New lead saved: " + incoming);
        }

    }

    private void updateLead(Lead existing, Lead incoming) {
        // Check if territory has changed for duplicate lead
        boolean territoryChanged = !existing.getTerritory().getCode().equals(incoming.getTerritory().getCode());

        /*if (existing.getStatus() == LeadStatus.QUEUED) {
            // remove from old queue and add to new based on new territory
        }*/

        // Reset lead assignment if territory has changed
        if (territoryChanged) {
            System.out.println("Territory change detected for " + existing);
            if (existing.getAssignedTo() != null) {
                existing.getAssignedTo().getActiveLeads().remove(existing);
                existing.setAssignedTo(null);
            }
            existing.setStatus(LeadStatus.NEW);
        }

        // Update existing lead with new lead info
        leadDb.update(existing, incoming);
        System.out.println("Updated existing lead instance to " + incoming);
    }

    public List<Lead> getAllLeads() {
        return new ArrayList<>(leadDb.getAll());
    }
}