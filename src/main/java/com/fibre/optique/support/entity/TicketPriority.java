package com.fibre.optique.support.entity;

public enum TicketPriority {
    BASSE,
    MOYENNE,
    HAUTE,
    CRITIQUE;

    /**
     * SLA deadline in hours per priority level.
     */
    public int slaHeures() {
        return switch (this) {
            case CRITIQUE -> 4;
            case HAUTE    -> 8;
            case MOYENNE  -> 24;
            case BASSE    -> 48;
        };
    }
}
