package noa.ignite.assignment.parse;

import noa.ignite.assignment.model.Lead;

import java.util.Map;

public abstract class Source {

    protected String read(Map<String, Object> data, String key) {
        if (data == null || data.get(key) == null) {
            return "";
        }
        return String.valueOf(data.get(key)).trim();
    }

    public abstract Lead process(Map<String, Object> data);
}