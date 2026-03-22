package noa.ignite.assignment.util;

import noa.ignite.assignment.model.Lead;
import noa.ignite.assignment.model.SalesRep;
import noa.ignite.assignment.model.Territory;
import noa.ignite.assignment.model.TerritoryType;

public final class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidLead(Lead lead) {
        if (lead == null) return false;
        return isValidString(lead.getEmail()) &&
                isValidString(lead.getFirstName()) &&
                isValidString(lead.getLastName()) &&
                isValidString(lead.getCompany()) &&
                isValidTerritory(lead.getTerritory()) &&
                lead.getSource() != null;
    }

    public static boolean isValidRep(SalesRep rep) {
        if (rep == null) return false;
        return isValidString(rep.getEmail()) &&
                isValidString(rep.getFirstName()) &&
                isValidString(rep.getLastName()) &&
                isValidTerritory(rep.getTerritory());
    }

    public static boolean isValidTerritory(Territory territory) {
        return territory != null && territory.getCode() != TerritoryType.UNKNOWN;
    }
}