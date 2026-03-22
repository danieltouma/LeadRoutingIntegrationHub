package noa.ignite.assignment.model;

import java.util.Objects;

public class Lead {
    private String firstName;
    private String lastName;
    private String email;
    private String company;
    private Territory territory;
    private LeadSource source;
    private LeadStatus status = LeadStatus.NEW;
    private SalesRep assignedTo;

    public Lead(String firstName, String lastName, String email, String company, Territory territory, LeadSource source) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.company = company;
        this.territory = territory;
        this.source = source;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public Territory getTerritory() { return territory; }
    public void setTerritory(Territory territory) { this.territory = territory; }

    public LeadSource getSource() { return source; }
    public void setSource(LeadSource source) { this.source = source; }

    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }

    public SalesRep getAssignedTo() { return assignedTo; }
    public void setAssignedTo(SalesRep assignedTo) { this.assignedTo = assignedTo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lead lead = (Lead) o;
        return Objects.equals(firstName, lead.firstName) && Objects.equals(lastName, lead.lastName) && Objects.equals(email, lead.email) && Objects.equals(company, lead.company);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, company);
    }

    @Override
    public String toString() {
        return String.format("Lead[email=%s, name=%s %s, company=%s]",
                safeLowerTrim(email),
                safeLowerTrim(firstName),
                safeLowerTrim(lastName),
                safeLowerTrim(company));
    }

    private String safeLowerTrim(String value) {
        if (value == null) {
            return "null";
        }
        return value.trim().toLowerCase();
    }
}