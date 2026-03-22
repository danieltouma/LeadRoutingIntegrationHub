package noa.ignite.assignment.distribution;

import noa.ignite.assignment.data.LeadStore;
import noa.ignite.assignment.data.SalesRepStore;
import noa.ignite.assignment.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalesRepManagerTest {

    private SalesRepStore salesRepStore;
    private RegionalRepQueue regionalRepQueue;
    private LeadManager leadManager;
    private SalesRepManager salesRepManager;
    private Territory usTerritory;

    @BeforeEach
    void setUp() {
        salesRepStore = new SalesRepStore();
        regionalRepQueue = new RegionalRepQueue();
        LeadStore leadStore = new LeadStore();

        // Initialize dependencies
        leadManager = new LeadManager(leadStore, regionalRepQueue);
        salesRepManager = new SalesRepManager(salesRepStore, regionalRepQueue, leadManager);

        usTerritory = new Territory(TerritoryType.US);
    }

    @Test
    void registerNewRep_ShouldAddRepToStoreAndQueue() {
        SalesRep alice = new SalesRep("Alice", "Doe", "alice@test.com", usTerritory);

        salesRepManager.registerNewRep(alice);

        // Verify it exists in the store
        assertTrue(salesRepStore.exists("alice@test.com"));

        // Verify it is available in the distribution queue
        assertEquals(alice, regionalRepQueue.findBestAvailableRep(usTerritory));
    }

    @Test
    void registerNewRep_ShouldRejectDuplicates() {
        SalesRep alice1 = new SalesRep("Alice", "Doe", "alice@test.com", usTerritory);
        SalesRep alice2 = new SalesRep("Alice", "Duplicate", "alice@test.com", usTerritory);

        salesRepManager.registerNewRep(alice1);
        salesRepManager.registerNewRep(alice2);

        // Should only have one rep in the system
        assertEquals(1, salesRepManager.getAllActiveReps().size());
    }

    @Test
    void terminateRep_ShouldPerformFullSystemCleanup() {
        // 1. Setup a rep with an active lead
        SalesRep bob = new SalesRep("Bob", "Smith", "bob@test.com", usTerritory);
        salesRepManager.registerNewRep(bob);

        Lead activeLead = new Lead("Test", "Lead", "lead@test.com", "Corp", usTerritory, LeadSource.WEBHOOK);
        activeLead.setStatus(LeadStatus.ASSIGNED);
        activeLead.setAssignedTo(bob);
        bob.getActiveLeads().add(activeLead);

        // 2. Terminate the rep
        salesRepManager.terminateRep(bob);

        // 3. Verify they are gone from the administrative store
        assertFalse(salesRepStore.exists("bob@test.com"));

        // 4. Verify they are gone from the distribution engine
        assertNull(regionalRepQueue.findBestAvailableRep(usTerritory));

        // 5. Verify the lead was recycled (Reset to NEW and unassigned)
        assertEquals(LeadStatus.NEW, activeLead.getStatus(), "Lead should be reset to NEW after rep termination");
        assertNull(activeLead.getAssignedTo(), "Lead should no longer have an assigned rep");
        assertTrue(bob.getActiveLeads().isEmpty(), "Rep's local active list should be cleared");
    }

    @Test
    void getAllActiveReps_ShouldReturnDefensiveCopy() {
        SalesRep alice = new SalesRep("Alice", "Doe", "alice@test.com", usTerritory);
        salesRepManager.registerNewRep(alice);

        List<SalesRep> reps = salesRepManager.getAllActiveReps();

        // Attempt to modify the returned list
        reps.clear();

        // The internal state should remain untouched
        assertEquals(1, salesRepManager.getAllActiveReps().size(), "Internal store should be protected from external list modification");
    }
}