package dslab.connection.types;

/**
 * Enum for the different exchange types.
 */
public enum ExchangeType {
    DEFAULT("default"),
    DIRECT("direct"),
    FANOUT("fanout"),
    TOPIC("topic"),
    ;
    private final String value;
    ExchangeType(String s) {
        this.value = s;
    }

    public String toString() {return value;}
}
