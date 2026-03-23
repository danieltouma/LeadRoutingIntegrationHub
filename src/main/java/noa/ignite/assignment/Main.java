package noa.ignite.assignment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import noa.ignite.assignment.core.AssignmentSystem;
import noa.ignite.assignment.distribution.SalesRepManager;
import noa.ignite.assignment.ingestion.LeadProcessor;
import noa.ignite.assignment.model.*;
import java.io.File;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        AssignmentSystem system = new AssignmentSystem();
        var hub = system.getLeadIngestionHub();
        var manager = system.getSalesRepManager();
        var engine = system.getLeadAssignmentEngine();

        // ObjectMapper (Jackson library)
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("=== IGNITE SYSTEM: JACKSON LIBRARY INGESTION ===");

        // Setup Reps
        manager.registerNewRep(new SalesRep("Alice", "Doe", "alice@ignite.com", new Territory(TerritoryType.US)));
        manager.registerNewRep(new SalesRep("Bob", "Burgers", "bob@ignite.com", new Territory(TerritoryType.EU)));
        manager.registerNewRep(new SalesRep("Steve", "Johnsson", "stev@ignite.com", new Territory(TerritoryType.US)));

        // Phase 1: Webhook JSON (Flat List)
        // Jackson maps the file directly into a List of Maps
        System.out.println("\n[WEBHOOK] Reading leads.json with Jackson...");
        List<Map<String, Object>> webhookLeads = mapper.readValue(
                new File("src/main/resources/webhook.json"),
                new TypeReference<List<Map<String, Object>>>() {}
        );
        webhookLeads.forEach(hub::fromWebhook);

        // Phase 2: CSV (Still manual since Jackson is for JSON)
        System.out.println("[CSV] Reading leads.csv...");
        List<String> csvLines = Files.readAllLines(Paths.get("src/main/resources/csv_bulk.csv"));
        ingestCsv(csvLines, hub);

        // Phase 3: API JSON (Nested List)
        // Jackson automatically handles the nested objects (user, name, etc.)
        System.out.println("[API] Reading api_response.json with Jackson...");
        List<Map<String, Object>> apiLeads = mapper.readValue(
                new File("src/main/resources/third_party_api.json"),
                new TypeReference<List<Map<String, Object>>>() {}
        );
        apiLeads.forEach(hub::fromThirdPartyApi);

        // Phase 4: Distribution
        System.out.println("\n[ENGINE] Processing unique leads...");
        engine.processAll();

        // Phase 5: Report
        printFinalReport(manager, system.getLeadProcessor());
    }

    private static void ingestCsv(List<String> lines, noa.ignite.assignment.ingestion.LeadIngestionHub hub) {
        if (lines.size() < 2) return;
        String[] headers = lines.get(0).split(",");
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(",", -1);
            Map<String, Object> row = new HashMap<>();
            for (int j = 0; j < headers.length; j++) {
                row.put(headers[j].trim(), j < values.length ? values[j].trim() : "");
            }
            hub.fromCsvRow(row);
        }
    }

    private static void printFinalReport(SalesRepManager manager, LeadProcessor processor) {
        System.out.println("\n============================================");
        System.out.println("              FINAL SYSTEM REPORT            ");
        System.out.println("============================================\n");
        manager.getAllActiveReps().forEach(rep -> {
            System.out.printf("- %s (%s): %d/5 assigned\n",
                    rep.getFirstName(), rep.getTerritory().getCode(), rep.getActiveLeads().size());
        });
        System.out.println("\nTotal Leads: " + processor.getAllLeads().size());
        System.out.println("In Queue   : " + processor.getAllLeads().stream().filter(l -> l.getStatus() == LeadStatus.QUEUED).count());
    }
}