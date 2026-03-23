package noa.ignite.assignment.distribution;

import noa.ignite.assignment.util.ValidationUtils;
import noa.ignite.assignment.model.SalesRep;
import noa.ignite.assignment.data.SalesRepStore;

import java.util.ArrayList;
import java.util.List;

public class SalesRepManager {
    private final SalesRepStore salesRepStore;
    private final RegionalRepQueue regionalRepQueue;
    private final LeadManager leadManager;

    public SalesRepManager(SalesRepStore salesRepStore, RegionalRepQueue regionalRepQueue, LeadManager leadManager) {
        this.salesRepStore = salesRepStore;
        this.regionalRepQueue = regionalRepQueue;
        this.leadManager = leadManager;
    }

    public synchronized void registerNewRep(SalesRep rep) {
        // Basic field validation check
        if (!ValidationUtils.isValidRep(rep)) {
            System.out.println("SalesRep missing required fields.");
            return;
        }

        // Prevent duplicate registration based on email
        if (salesRepStore.exists(rep.getEmail())) {
            System.out.println(rep + " already exists.");
            return;
        }

        // Update both the salesRepStore and the regionalRepQueue
        salesRepStore.add(rep);
        regionalRepQueue.registerRep(rep);
        System.out.println(rep + " is now active in the system.");
    }

    public synchronized void terminateRep(SalesRep rep) {
        if (rep == null || rep.getEmail() == null) return;

        // Fetch the fresh state from the store to ensure data consistency
        SalesRep repToRemove = salesRepStore.findByEmail(rep.getEmail());

        if (repToRemove != null) {
            // Remove the rep from persistent storage
            salesRepStore.remove(repToRemove.getEmail());

            // Remove from the regional distribution queue
            regionalRepQueue.unregisterRep(repToRemove);

            // Re queue any leads that were currently assigned to this rep
            leadManager.resetActiveLeads(repToRemove);

            System.out.println(repToRemove + " fully offboarded.");
        } else {
            System.out.println("Could not find rep in the system.");
        }
    }

    public List<SalesRep> getAllActiveReps() {
        return new ArrayList<>(salesRepStore.getAll());
    }
}