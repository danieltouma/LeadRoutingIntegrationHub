package noa.ignite.assignment.model;

public class Territory {
    private TerritoryType code;

    public Territory(TerritoryType code) {
        this.code = code;
    }

    public TerritoryType getCode() {
        return code;
    }

    public void setCode(TerritoryType code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code.toString();
    }
}