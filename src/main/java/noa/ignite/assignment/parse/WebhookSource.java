package noa.ignite.assignment.parse;

import noa.ignite.assignment.ingestion.LeadProcessor;
import noa.ignite.assignment.model.*;
import java.util.Map;

public class WebhookSource extends Source {
    public WebhookSource(LeadProcessor processor) { super(processor); }

    @Override
    public void process(Map<String, Object> data) {
        Lead lead = new Lead(
                read(data, "firstName"),
                read(data, "lastName"),
                read(data, "emailAddress"),
                read(data, "companyName"),
                new Territory(TerritoryType.fromString(read(data, "country"))),
                LeadSource.WEBHOOK
        );
        processor.processIncomingLead(lead);
    }

}