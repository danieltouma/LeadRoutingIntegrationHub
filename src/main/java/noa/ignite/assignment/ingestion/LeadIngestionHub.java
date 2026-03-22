package noa.ignite.assignment.ingestion;

import noa.ignite.assignment.parse.CsvSource;
import noa.ignite.assignment.parse.ThirdPartyApiSource;
import noa.ignite.assignment.parse.WebhookSource;

import java.util.Map;

public class LeadIngestionHub {
    private final WebhookSource webhook;
    private final CsvSource csv;
    private final ThirdPartyApiSource api;
    private final LeadProcessor processor;

    public LeadIngestionHub(LeadProcessor processor) {
        this.webhook = new WebhookSource();
        this.csv = new CsvSource();
        this.api = new ThirdPartyApiSource();
        this.processor = processor;
    }

    public void fromWebhook(Map<String, Object> data) {
        processor.processIncomingLead(webhook.process(data));

    }

    public void fromCsvRow(Map<String, Object> data) {
        processor.processIncomingLead(csv.process(data));
    }

    public void fromThirdPartyApi(Map<String, Object> data) {
        processor.processIncomingLead(api.process(data));
    }
}