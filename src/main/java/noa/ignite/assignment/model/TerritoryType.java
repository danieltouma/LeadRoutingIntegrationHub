package noa.ignite.assignment.model;

public enum TerritoryType {
    US,
    EU,
    UNKNOWN;

    public static TerritoryType fromString(String code) {
        if (code == null || code.isEmpty()) return UNKNOWN;
        try {
            return TerritoryType.valueOf(code.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}