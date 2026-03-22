package noa.ignite.assignment.parse;

import noa.ignite.assignment.ingestion.LeadProcessor;
import noa.ignite.assignment.model.*;
import java.util.Map;

public class ThirdPartyApiSource extends Source {
    public ThirdPartyApiSource(LeadProcessor processor) { super(processor); }

    @Override
    public void process(Map<String, Object> data) {
        Map<String, Object> user = (Map<String, Object>) data.getOrDefault("user", Map.of());
        Map<String, Object> name = (Map<String, Object>) user.getOrDefault("name", Map.of());
        Map<String, Object> contact = (Map<String, Object>) user.getOrDefault("contact", Map.of());
        Map<String, Object> location = (Map<String, Object>) data.getOrDefault("location", Map.of());

        Lead lead = new Lead(
                read(name, "first"),
                read(name, "last"),
                read(contact, "email"),
                read(data, "employer"),
                new Territory(TerritoryType.fromString(read(location, "countryCode"))),
                LeadSource.THIRD_PARTY_API
        );
        processor.processIncomingLead(lead);
    }
}