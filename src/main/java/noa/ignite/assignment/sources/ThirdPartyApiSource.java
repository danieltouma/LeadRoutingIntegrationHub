package noa.ignite.assignment.sources;

import noa.ignite.assignment.model.*;
import java.util.Collections;
import java.util.Map;

public class ThirdPartyApiSource extends Source {
    public ThirdPartyApiSource() {}

    @Override
    public Lead process(Map<String, Object> data) {
        // Retrieve nested maps but force them to be empty maps if the value is null or missing
        Map<String, Object> user = safeMap(data, "user");
        Map<String, Object> name = safeMap(user, "name");
        Map<String, Object> contact = safeMap(user, "contact");
        Map<String, Object> location = safeMap(data, "location");

        return new Lead(
                read(name, "first"),
                read(name, "last"),
                read(contact, "email"),
                read(data, "employer"),
                new Territory(TerritoryType.fromString(read(location, "countryCode"))),
                LeadSource.THIRD_PARTY_API
        );
    }


     // Helper method that ensures we never encounter a NullPointerException during nesting.
     // If the key is missing or the value is null, it returns an empty immutable map.
    private Map<String, Object> safeMap(Map<String, Object> parent, String key) {
        Object val = parent.get(key);
        if (val instanceof Map) {
            return (Map<String, Object>) val;
        }
        return Collections.emptyMap();
    }
}