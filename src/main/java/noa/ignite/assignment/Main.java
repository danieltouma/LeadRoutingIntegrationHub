package noa.ignite.assignment;

import noa.ignite.assignment.core.AssignmentSystem;
import noa.ignite.assignment.model.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // 1. Initialize the entire system stack
        AssignmentSystem system = new AssignmentSystem();
        var hub = system.getLeadIngestionHub();
        var manager = system.getSalesRepManager();
        var engine = system.getLeadAssignmentEngine();

        System.out.println("=== IGNITE LEAD ASSIGNMENT SYSTEM STARTING ===");

        // 2. Setup initial Sales Representatives
        manager.registerNewRep(new SalesRep("Alice", "Doe", "alice@ignite.com", new Territory(TerritoryType.US)));
        manager.registerNewRep(new SalesRep("Denise", "Smith", "denise@ignite.com", new Territory(TerritoryType.US)));
        manager.registerNewRep(new SalesRep("Bob", "Burgers", "bob@ignite.com", new Territory(TerritoryType.EU)));

        // 3. Create a scheduler to simulate real-world activity
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // TASK A: Simulate Incoming Traffic (Ingest 1 lead every 1 second)
        List<String> names = Arrays.asList("Clark", "Bruce", "Diana", "Barry", "Hal", "Arthur", "Victor");
        scheduler.scheduleAtFixedRate(() -> {
            String name = names.get(new Random().nextInt(names.size()));
            TerritoryType region = (new Random().nextBoolean()) ? TerritoryType.US : TerritoryType.EU;

            hub.fromWebhook(Map.of(
                    "firstName", name,
                    "lastName", "Lead",
                    "emailAddress", name.toLowerCase() + System.nanoTime() + "@prototype.com",
                    "companyName", "AutoCorp",
                    "country", region.toString()
            ));
        }, 0, 1, TimeUnit.SECONDS);

        // TASK B: The System Heartbeat (Run distribution every 2 seconds)
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n[SYSTEM HEARTBEAT] Running distribution sweep...");
            engine.processAll();
        }, 1, 2, TimeUnit.SECONDS);

        // 4. Let the prototype run for 10 seconds to observe capacity and queuing
        System.out.println("Prototype is running. Watch logs for assignments and queue alerts...");
        Thread.sleep(10000);

        // 5. Shutdown and Final Report
        System.out.println("\n=== SHUTTING DOWN PROTOTYPE ===");
        scheduler.shutdown();
        if (scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
            printFinalReport(manager, system.getLeadProcessor());
        }
    }

    private static void printFinalReport(noa.ignite.assignment.distribution.SalesRepManager manager,
                                         noa.ignite.assignment.ingestion.LeadProcessor processor) {
        System.out.println("\n============================================");
        System.out.println("              FINAL SYSTEM REPORT            ");
        System.out.println("============================================\n");

        System.out.println("SALES REP STATUS:");
        manager.getAllActiveReps().forEach(rep -> {
            System.out.printf("- %s (%s): %d/5 leads assigned\n",
                    rep.getFirstName(), rep.getTerritory().getCode(), rep.getActiveLeads().size());
            rep.getActiveLeads().forEach(l -> System.out.println("    * " + l.getEmail()));
        });

        long totalLeads = processor.getAllLeads().size();
        long assignedLeads = processor.getAllLeads().stream()
                .filter(l -> l.getStatus() == LeadStatus.ASSIGNED).count();
        long queuedLeads = processor.getAllLeads().stream()
                .filter(l -> l.getStatus() == LeadStatus.QUEUED).count();

        System.out.println("\nGLOBAL STATISTICS:");
        System.out.println("Total Ingested : " + totalLeads);
        System.out.println("Total Assigned : " + assignedLeads);
        System.out.println("Total Queued   : " + queuedLeads);
        System.out.println("\n============================================");
    }
}