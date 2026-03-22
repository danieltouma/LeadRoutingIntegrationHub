package noa.ignite.assignment.model;

import java.util.ArrayList;
import java.util.List;

public class SalesRep {
    private static final int MAX_CAPACITY = 5;
    private String firstName;
    private String lastName;
    private String email;
    private Territory territory;
    private List<Lead> activeLeads = new ArrayList<>();
    private long lastAssignmentTime = 0;

    public SalesRep(String firstName, String lastName, String email, Territory territory) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.territory = territory;
    }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getEmail() { return email; }

    public List<Lead> getActiveLeads() { return activeLeads; }

    public Territory getTerritory() { return territory; }

    public long getLastAssignmentTime() { return lastAssignmentTime; }
    public void setLastAssignmentTime(long lastAssignmentTime) { this.lastAssignmentTime = lastAssignmentTime; }

    public boolean hasCapacity() {
        return this.activeLeads.size() < MAX_CAPACITY;
    }

    @Override
    public String toString() {
        return String.format("SalesRep[email=%s, name=%s %s]",
                safeLowerTrim(email),
                safeLowerTrim(firstName),
                safeLowerTrim(lastName));
    }

    private String safeLowerTrim(String value) {
        if (value == null) {
            return "null";
        }
        return value.trim().toLowerCase();
    }
}