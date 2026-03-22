package noa.ignite.assignment.distribution;

import noa.ignite.assignment.util.ValidationUtils;
import noa.ignite.assignment.model.*;
import noa.ignite.assignment.data.LeadStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LeadManager {
    private final LeadStore leadDb;
    private final RegionalRepQueue regionalRepQueue;
    private final Map<TerritoryType, Queue<Lead>> regionalWaitingQueues = new ConcurrentHashMap<>();

    public LeadManager(LeadStore leadDb, RegionalRepQueue regionalRepQueue) {
        this.leadDb = leadDb;
        this.regionalRepQueue = regionalRepQueue;

        // Pre create queues for known territories at startup.
        for (TerritoryType type : TerritoryType.values()) {
            if (type != TerritoryType.UNKNOWN) {
                regionalWaitingQueues.put(type, new ConcurrentLinkedQueue<>());
            }
        }
    }

    public synchronized void processAll() {
        // 1. Always prioritize the backlog first (Fairness/FIFO)
        drainWaitingQueues();

        // 2. Fetch leads that are marked as NEW and ready for their first assignment
        List<Lead> newLeads = leadDb.getAll().stream()
                .filter(l -> l.getStatus() == LeadStatus.NEW)
                .toList();

        if (!newLeads.isEmpty()) {
            System.out.println("Processing new leads.");
            for (Lead lead : newLeads) {
                tryToRoute(lead);
            }
        }
    }

    private void drainWaitingQueues() {
        boolean leadsWereProcessed = false;

        // Get the queues per territory
        for (TerritoryType type : regionalWaitingQueues.keySet()) {
            Queue<Lead> queue = regionalWaitingQueues.get(type);

            while (!queue.isEmpty()) {
                // Peek first to see if the lead is still valid for this queue
                Lead lead = queue.peek();

                // Safety check: skip if lead was updated/assigned elsewhere
                if (lead.getStatus() != LeadStatus.QUEUED || lead.getTerritory().getCode() != type) {
                    queue.poll();
                    continue;
                }

                SalesRep rep = regionalRepQueue.findBestAvailableRep(lead.getTerritory());
                if (rep != null) {
                    queue.poll(); // Successfully removing from the queue
                    assign(lead, rep);
                    leadsWereProcessed = true;
                } else {
                    // No capacity in this region; stop draining this specific queue
                    break;
                }
            }
        }

        if (leadsWereProcessed) {
            System.out.println("Successfully assigned leads from waiting queues.");
        }
    }

    private void tryToRoute(Lead lead) {
        if (!ValidationUtils.isValidTerritory(lead.getTerritory())) {
            System.out.println("Cannot route " + lead + " - Territory is UNKNOWN.");
            return;
        }

        SalesRep bestRep = regionalRepQueue.findBestAvailableRep(lead.getTerritory());

        if (bestRep != null) {
            assign(lead, bestRep);
        } else {
            // No capacity available: move to the regional waiting room
            lead.setStatus(LeadStatus.QUEUED);
            TerritoryType type = lead.getTerritory().getCode();

            // Add to the queue
            regionalWaitingQueues.get(type).add(lead);
            System.out.println("No capacity in " + type + " for " + lead + ". Status: QUEUED.");
        }
    }

    private void assign(Lead lead, SalesRep rep) {
        lead.setAssignedTo(rep);
        lead.setStatus(LeadStatus.ASSIGNED);

        // Update the Rep's state
        rep.getActiveLeads().add(lead);
        rep.setLastAssignmentTime(System.nanoTime());

        // Notify the queue to move this rep to the back of the line
        regionalRepQueue.updateRepPosition(rep);

        System.out.println(lead + " assigned to " + rep);
    }

    public synchronized void resetActiveLeads(SalesRep rep) {
        List<Lead> leadsToMove = new ArrayList<>(rep.getActiveLeads());
        if (leadsToMove.isEmpty()) return;

        System.out.println("Re-queueing " + leadsToMove.size() + " leads from " + rep.getFirstName());

        for (Lead lead : leadsToMove) {
            lead.setAssignedTo(null);
            lead.setStatus(LeadStatus.NEW); // Set to NEW so processAll picks them up
        }

        rep.getActiveLeads().clear();
    }
}