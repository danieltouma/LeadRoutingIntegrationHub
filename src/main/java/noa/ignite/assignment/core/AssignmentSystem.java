package noa.ignite.assignment.core;

import noa.ignite.assignment.data.*;
import noa.ignite.assignment.distribution.SalesRepManager;
import noa.ignite.assignment.ingestion.LeadIngestionHub;
import noa.ignite.assignment.ingestion.LeadProcessor;
import noa.ignite.assignment.distribution.RegionalRepQueue;
import noa.ignite.assignment.distribution.LeadManager;

public class AssignmentSystem {
    private final LeadIngestionHub leadIngestionHub;
    private final LeadManager leadManager;
    private final SalesRepManager salesRepManager;
    private final LeadProcessor leadProcessor;

    public AssignmentSystem() {
        // 1. Initialize data stores (Shared by all components below)
        SalesRepStore salesStore = new SalesRepStore();
        LeadStore leadStore = new LeadStore();

        // 2. Initialize the processor (Requires leadStore to save data and check for duplicates)
        this.leadProcessor = new LeadProcessor(leadStore);

        // 3. Initialize the hub (Uses the processor to submit sanitized data from various sources)
        this.leadIngestionHub = new LeadIngestionHub(this.leadProcessor);

        // 4. Initialize the queue that manages regional waiting lists for sales representatives
        RegionalRepQueue regionalRepQueue = new RegionalRepQueue();

        // 5. Initialize the lead manager (Requires leadStore to read leads and the queue to find reps)
        this.leadManager = new LeadManager(leadStore, regionalRepQueue);

        // 6. Initialize the sales rep manager (Connects store, queue, and engine for rep operations)
        this.salesRepManager = new SalesRepManager(salesStore, regionalRepQueue, leadManager);
    }

    public LeadIngestionHub getLeadIngestionHub() {
        return leadIngestionHub;
    }

    public SalesRepManager getSalesRepManager() {
        return salesRepManager;
    }

    public LeadProcessor getLeadProcessor() {
        return leadProcessor;
    }

    public LeadManager getLeadAssignmentEngine() {
        return leadManager;
    }
}