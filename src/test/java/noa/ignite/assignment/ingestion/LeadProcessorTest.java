package noa.ignite.assignment.ingestion;

import noa.ignite.assignment.data.LeadStore;
import noa.ignite.assignment.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LeadProcessorTest {

    private LeadStore leadStore;
    private LeadProcessor processor;

    @BeforeEach
    void setUp() {
        leadStore = new LeadStore();
        processor = new LeadProcessor(leadStore);
    }

    @Test
    void processIncomingLead_ShouldSaveNewLead() {
        Lead newLead = new Lead("Bruce", "Wayne", "bruce@wayne.com", "Wayne Ent",
                new Territory(TerritoryType.US), LeadSource.WEBHOOK);

        processor.processIncomingLead(newLead);

        List<Lead> allLeads = processor.getAllLeads();
        assertEquals(1, allLeads.size(), "Store should contain exactly one lead");
        assertEquals("bruce@wayne.com", allLeads.get(0).getEmail());
    }

    @Test
    void processIncomingLead_ShouldUpdateExistingLeadAndNotDuplicate() {
        Lead firstEntry = new Lead("Clark", "Kent", "clark@daily.com", "Daily Planet",
                new Territory(TerritoryType.US), LeadSource.WEBHOOK);
        processor.processIncomingLead(firstEntry);

        // Same person, but now coming from CSV with a slight name update
        Lead duplicateEntry = new Lead("Kal-El", "Kent", "clark@daily.com", "Daily Planet",
                new Territory(TerritoryType.US), LeadSource.CSV);
        processor.processIncomingLead(duplicateEntry);

        List<Lead> allLeads = processor.getAllLeads();
        assertEquals(1, allLeads.size(), "Should still only have one lead due to deduplication");
        assertEquals("Kal-El", allLeads.get(0).getFirstName(), "Existing lead should have been updated with new data");
    }

    @Test
    void processIncomingLead_ShouldResetStatusAndRepOnTerritoryChange() {
        // 1. Create a lead and manually assign a SalesRep and status
        Lead lead = new Lead("Diana", "Prince", "diana@themyscira.com", "Justice League",
                new Territory(TerritoryType.EU), LeadSource.WEBHOOK);

        // Mock a SalesRep
        SalesRep rep = new SalesRep("Steve", "Trevor", "steve@army.mil", new Territory(TerritoryType.EU));
        lead.setAssignedTo(rep);
        lead.setStatus(LeadStatus.ASSIGNED);
        rep.getActiveLeads().add(lead);

        leadStore.save(lead);

        // 2. Incoming update for the same lead but with a NEW territory (US)
        Lead update = new Lead("Diana", "Prince", "diana@themyscira.com", "Justice League",
                new Territory(TerritoryType.US), LeadSource.WEBHOOK);

        processor.processIncomingLead(update);

        // 3. Verify the lead is "back to square one"
        Lead processed = leadStore.getAll().get(0);
        assertEquals(TerritoryType.US, processed.getTerritory().getCode());
        assertEquals(LeadStatus.NEW, processed.getStatus(), "Status should reset to NEW after territory change");
        assertNull(processed.getAssignedTo(), "Lead should be unassigned after territory change");
        assertTrue(rep.getActiveLeads().isEmpty(), "Rep's active list should no longer contain this lead");
    }

    @Test
    void processIncomingLead_ShouldRejectInvalidLead() {
        // Lead missing an email address
        Lead invalidLead = new Lead("John", "Doe", "", "Missing Email Corp",
                new Territory(TerritoryType.US), LeadSource.WEBHOOK);

        processor.processIncomingLead(invalidLead);

        assertTrue(processor.getAllLeads().isEmpty(), "Invalid lead should not be saved to the database");
    }

    @Test
    void getAllLeads_ShouldReturnCopyOfList() {
        Lead lead = new Lead("Barry", "Allen", "barry@centralcity.com", "Police Dept",
                new Territory(TerritoryType.US), LeadSource.WEBHOOK);
        processor.processIncomingLead(lead);

        List<Lead> leads = processor.getAllLeads();

        assertNotSame(leads, leadStore.getAll(), "Should return a new list instance for encapsulation");
        assertEquals(1, leads.size());
    }
}