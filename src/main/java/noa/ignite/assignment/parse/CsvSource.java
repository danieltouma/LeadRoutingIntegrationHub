package noa.ignite.assignment.parse;

import noa.ignite.assignment.model.*;
import java.util.Map;

public class CsvSource extends Source {
    public CsvSource() { }

    @Override
    public Lead process(Map<String, Object> data) {
        return new Lead(
                read(data, "First_Name"),
                read(data, "Last_Name"),
                read(data, "Email"),
                read(data, "Company"),
                new Territory(TerritoryType.fromString(read(data, "Region"))),
                LeadSource.CSV
        );
    }
}