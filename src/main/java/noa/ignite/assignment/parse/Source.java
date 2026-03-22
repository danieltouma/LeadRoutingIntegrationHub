package noa.ignite.assignment.parse;

import noa.ignite.assignment.ingestion.LeadProcessor;

import java.util.Map;

public abstract class Source {
    protected final LeadProcessor processor;

    protected Source(LeadProcessor processor) {
        this.processor = processor;
    }

    protected String read(Map<String, Object> data, String key) {
        if (data == null || data.get(key) == null) {
            return "";
        }
        return String.valueOf(data.get(key)).trim();
    }

    public abstract void process(Map<String, Object> data);
}