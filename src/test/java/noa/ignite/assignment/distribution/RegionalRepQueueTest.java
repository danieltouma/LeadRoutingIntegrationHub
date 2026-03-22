package noa.ignite.assignment.distribution;

import noa.ignite.assignment.model.SalesRep;
import noa.ignite.assignment.model.Territory;
import noa.ignite.assignment.model.TerritoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionalRepQueueTest {

    private RegionalRepQueue repQueue;
    private Territory usTerritory;

    @BeforeEach
    void setUp() {
        repQueue = new RegionalRepQueue();
        usTerritory = new Territory(TerritoryType.US);
    }

    @Test
    void registerRep_ShouldAddRepToCorrectRegionalQueue() {
        SalesRep alice = new SalesRep("Alice", "Doe", "alice@test.com", usTerritory);

        repQueue.registerRep(alice);

        SalesRep found = repQueue.findBestAvailableRep(usTerritory);
        assertEquals(alice, found, "Alice should be found in the US queue after registration");
    }

    @Test
    void unregisterRep_ShouldRemoveRepFromQueue() {
        SalesRep alice = new SalesRep("Alice", "Doe", "alice@test.com", usTerritory);
        repQueue.registerRep(alice);

        repQueue.unregisterRep(alice);

        SalesRep found = repQueue.findBestAvailableRep(usTerritory);
        assertNull(found, "Queue should be empty after Alice is unregistered");
    }

    @Test
    void findBestAvailableRep_ShouldRespectRoundRobinOrdering() {
        // 1. Create two reps
        SalesRep alice = new SalesRep("Alice", "Doe", "alice@test.com", usTerritory);
        SalesRep bob = new SalesRep("Bob", "Smith", "bob@test.com", usTerritory);

        // 2. Set Alice to be "older" (smaller timestamp) so she is first in line
        alice.setLastAssignmentTime(1000L);
        bob.setLastAssignmentTime(2000L);

        repQueue.registerRep(alice);
        repQueue.registerRep(bob);

        // 3. Alice should be picked first
        assertEquals(alice, repQueue.findBestAvailableRep(usTerritory), "Alice should be picked as she has the oldest assignment time");

        // 4. Update Alice's time to be the "newest"
        alice.setLastAssignmentTime(3000L);
        repQueue.updateRepPosition(alice);

        // 5. Now Bob should be picked first
        assertEquals(bob, repQueue.findBestAvailableRep(usTerritory), "Bob should now be first in line after Alice was updated");
    }

    @Test
    void findBestAvailableRep_ShouldSkipRepsAtFullCapacity() {
        SalesRep busyAlice = new SalesRep("Alice", "Doe", "alice@test.com", usTerritory);
        SalesRep availableBob = new SalesRep("Bob", "Smith", "bob@test.com", usTerritory);

        // Alice is first in line but FULL
        busyAlice.setLastAssignmentTime(100L);
        for (int i = 0; i < 5; i++) {
            busyAlice.getActiveLeads().add(null); // Fill capacity manually for the test
        }

        // Bob is second in line but has space
        availableBob.setLastAssignmentTime(200L);

        repQueue.registerRep(busyAlice);
        repQueue.registerRep(availableBob);

        // Execution: Alice should be skipped, Bob should be returned
        SalesRep selected = repQueue.findBestAvailableRep(usTerritory);
        assertEquals(availableBob, selected, "The queue should skip Alice because she is at full capacity");
    }

    @Test
    void updateRepPosition_ShouldForceReSort() {
        SalesRep alice = new SalesRep("Alice", "Doe", "alice@test.com", usTerritory);
        SalesRep bob = new SalesRep("Bob", "Smith", "bob@test.com", usTerritory);

        // Bob is initially first
        bob.setLastAssignmentTime(10L);
        alice.setLastAssignmentTime(50L);

        repQueue.registerRep(alice);
        repQueue.registerRep(bob);

        assertEquals(bob, repQueue.findBestAvailableRep(usTerritory));

        // Move Bob to the back
        bob.setLastAssignmentTime(100L);
        repQueue.updateRepPosition(bob);

        assertEquals(alice, repQueue.findBestAvailableRep(usTerritory), "Alice should move to the front after Bob's time was updated");
    }
}