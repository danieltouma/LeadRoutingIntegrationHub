package noa.ignite.assignment.ingestion;

import noa.ignite.assignment.data.LeadStore;
import noa.ignite.assignment.model.Lead;
import noa.ignite.assignment.model.LeadSource;
import noa.ignite.assignment.model.TerritoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LeadIngestionHubTest {

    private LeadStore leadStore;
    private LeadIngestionHub hub;

    @BeforeEach
    void setUp() {
        // Resetting the store before each test to ensure isolation
        leadStore = new LeadStore();
        LeadProcessor processor = new LeadProcessor(leadStore);
        hub = new LeadIngestionHub(processor);
    }

    @Test
    void fromWebhook_ShouldNormalizeAndSaveValidLead() {
        Map<String, Object> data = Map.of(
                "firstName", "Clark",
                "lastName", "Kent",
                "emailAddress", "clark@daily.com",
                "companyName", "Daily Planet",
                "country", "US"
        );

        hub.fromWebhook(data);

        assertFalse(leadStore.getAll().isEmpty(), "Lead should have been saved");
        Lead saved = leadStore.getAll().get(0);
        assertEquals("clark@daily.com", saved.getEmail());
        assertEquals(TerritoryType.US, saved.getTerritory().getCode());
        assertEquals(LeadSource.WEBHOOK, saved.getSource());
    }

    @Test
    void fromCsvRow_ShouldNormalizeAndSaveValidLead() {
        Map<String, Object> data = Map.of(
                "First_Name", "Bruce",
                "Last_Name", "Wayne",
                "Email", "bruce@wayne.com",
                "Company", "Wayne Ent",
                "Region", "US"
        );

        hub.fromCsvRow(data);

        assertFalse(leadStore.getAll().isEmpty(), "Lead should have been saved");
        Lead saved = leadStore.getAll().get(0);
        assertEquals("bruce@wayne.com", saved.getEmail());
        assertEquals("Wayne Ent", saved.getCompany());
        assertEquals(LeadSource.CSV, saved.getSource());
    }

    @Test
    void fromThirdPartyApi_ShouldHandleNestedDataCorrectly() {
        Map<String, Object> data = Map.of(
                "user", Map.of(
                        "name", Map.of("first", "Diana", "last", "Prince"),
                        "contact", Map.of("email", "diana@themyscira.com")
                ),
                "employer", "Justice League",
                "location", Map.of("countryCode", "EU")
        );

        hub.fromThirdPartyApi(data);

        assertFalse(leadStore.getAll().isEmpty(), "Lead should have been saved");
        Lead saved = leadStore.getAll().get(0);
        assertEquals("diana@themyscira.com", saved.getEmail());
        assertEquals(TerritoryType.EU, saved.getTerritory().getCode());
    }

    @Test
    void shouldDeduplicateBasedOnEmailAddress() {
        // 1. Ingest via Webhook
        hub.fromWebhook(Map.of(
                "firstName", "Hal", "lastName", "Jordan",
                "emailAddress", "hal@green.com", "companyName", "Ferris Air", "country", "US"
        ));

        // 2. Ingest same person via CSV with same email but updated company
        hub.fromCsvRow(Map.of(
                "First_Name", "Harold", "Last_Name", "Jordan",
                "Email", "hal@green.com", "Company", "Ferris Aircraft", "Region", "US"
        ));

        assertEquals(1, leadStore.getAll().size(), "Email deduplication failed");
        assertEquals("Ferris Aircraft", leadStore.getAll().get(0).getCompany(), "Existing lead should have been updated");
    }

    @Test
    void shouldDeduplicateBasedOnNameAndCompany() {
        // 1. Save a lead
        hub.fromWebhook(Map.of(
                "firstName", "Oliver", "lastName", "Queen",
                "emailAddress", "ollie@qc.com", "companyName", "Queen Corp", "country", "US"
        ));

        // 2. Ingest same name and company but a new email address
        hub.fromCsvRow(Map.of(
                "First_Name", "Oliver", "Last_Name", "Queen",
                "Email", "new-email@qc.com", "Company", "Queen Corp", "Region", "US"
        ));

        // Total leads should still be 1, but email should be updated to the latest one
        assertEquals(1, leadStore.getAll().size(), "Name+Company deduplication failed");
        assertEquals("new-email@qc.com", leadStore.getAll().get(0).getEmail());
    }

    @Test
    void shouldRejectLeadWithMissingRequiredFields() {
        // Missing firstName and emailAddress completely
        Map<String, Object> brokenData = Map.of(
                "lastName", "Kent",
                "companyName", "Daily Planet",
                "country", "US"
        );

        hub.fromWebhook(brokenData);

        assertTrue(leadStore.getAll().isEmpty(), "Lead with missing data should have been rejected by validation");
    }

    @Test
    void shouldRejectLeadWithUnknownTerritory() {
        Map<String, Object> invalidTerritoryData = Map.of(
                "firstName", "Jonn", "lastName", "Jonzz",
                "emailAddress", "martian@manhunter.com",
                "companyName", "Justice League",
                "country", "MARS" // This will map to TerritoryType.UNKNOWN
        );

        hub.fromWebhook(invalidTerritoryData);

        assertTrue(leadStore.getAll().isEmpty(), "Leads with unknown territory should not be saved in the system");
    }
}