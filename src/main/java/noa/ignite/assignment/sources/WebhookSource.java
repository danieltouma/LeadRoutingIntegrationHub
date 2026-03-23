package noa.ignite.assignment.sources;

import noa.ignite.assignment.model.*;
import java.util.Map;

public class WebhookSource extends Source {
    public WebhookSource() {}

    @Override
    public Lead process(Map<String, Object> data) {
        return new Lead(
                read(data, "firstName"),
                read(data, "lastName"),
                read(data, "emailAddress"),
                read(data, "companyName"),
                new Territory(TerritoryType.fromString(read(data, "country"))),
                LeadSource.WEBHOOK
        );
    }

}