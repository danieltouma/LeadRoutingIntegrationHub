package noa.ignite.assignment.ingestion;

import noa.ignite.assignment.parse.CsvSource;
import noa.ignite.assignment.parse.ThirdPartyApiSource;
import noa.ignite.assignment.parse.WebhookSource;

import java.util.Map;

public class LeadIngestionHub {
    private final WebhookSource webhook;
    private final CsvSource csv;
    private final ThirdPartyApiSource api;

    public LeadIngestionHub(LeadProcessor processor) {
        this.webhook = new WebhookSource(processor);
        this.csv = new CsvSource(processor);
        this.api = new ThirdPartyApiSource(processor);
    }

    public void fromWebhook(Map<String, Object> data) {
        webhook.process(data);
    }

    public void fromCsvRow(Map<String, Object> data) {
        csv.process(data);
    }

    public void fromThirdPartyApi(Map<String, Object> data) {
        api.process(data);
    }
}