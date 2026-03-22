package noa.ignite.assignment.distribution;

import noa.ignite.assignment.data.LeadStore;
import noa.ignite.assignment.data.SalesRepStore;
import noa.ignite.assignment.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeadManagerTest {

    private LeadStore leadStore;
    private SalesRepManager salesRepManager;
    private LeadManager leadManager;
    private SalesRep aliceUS;

    @BeforeEach
    void setUp() {
        leadStore = new LeadStore();
        RegionalRepQueue regionalRepQueue = new RegionalRepQueue();
        SalesRepStore salesRepStore = new SalesRepStore();

        // 1. Initialize LeadManager first
        leadManager = new LeadManager(leadStore, regionalRepQueue);

        // 2. Initialize SalesRepManager with the LeadManager instance
        salesRepManager = new SalesRepManager(salesRepStore, regionalRepQueue, leadManager);

        aliceUS = new SalesRep("Alice", "Doe", "alice@ignite.com", new Territory(TerritoryType.US));
        salesRepManager.registerNewRep(aliceUS);
    }

    @Test
    void processAll_ShouldQueueLeadsWhenRepCapacityIsFull() {
        // Fill Alice (5/5)
        for (int i = 0; i < 5; i++) {
            leadStore.save(new Lead("L", "N", "l" + i + "@t.com", "C", new Territory(TerritoryType.US), LeadSource.WEBHOOK));
        }
        leadManager.processAll();
        assertEquals(5, aliceUS.getActiveLeads().size());

        // Add 6th lead
        Lead overflow = new Lead("O", "V", "overflow@t.com", "C", new Territory(TerritoryType.US), LeadSource.WEBHOOK);
        leadStore.save(overflow);
        leadManager.processAll();

        assertEquals(LeadStatus.QUEUED, overflow.getStatus());

        // Free space
        aliceUS.getActiveLeads().remove(0);
        leadManager.processAll();

        assertEquals(5, aliceUS.getActiveLeads().size());
        assertEquals(LeadStatus.ASSIGNED, overflow.getStatus());
    }

    @Test
    void terminateRep_ShouldTriggerLeadResetViaLeadManager() {
        // 1. Assign leads to Alice
        leadStore.save(new Lead("L1", "N1", "1@t.com", "C", new Territory(TerritoryType.US), LeadSource.WEBHOOK));
        leadManager.processAll();
        assertFalse(aliceUS.getActiveLeads().isEmpty());

        // 2. Terminate Alice via the Manager
        salesRepManager.terminateRep(aliceUS);

        // 3. Verify the LeadManager's reset logic was triggered
        assertTrue(aliceUS.getActiveLeads().isEmpty(), "Leads should be cleared from the rep");
        assertEquals(LeadStatus.NEW, leadStore.getAll().get(0).getStatus(), "Lead status should be reset to NEW");
        assertNull(leadStore.getAll().get(0).getAssignedTo(), "Lead should be unassigned");
    }
}