package com.example.springbootapp.model;

/**
 * Enum for payment status values to ensure type safety and prevent typos.
 * Replaces string-based payment status to provide compile-time checking.
 */
public enum PaymentStatus {
    PENDING("pending", "Payment is pending"),
    PAID("paid", "Payment has been completed"),
    FAILED("failed", "Payment has failed"),
    CANCELLED("cancelled", "Payment was cancelled");

    private final String value;
    private final String description;

    PaymentStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Convert string value to PaymentStatus enum
     * @param value The string value
     * @return The corresponding PaymentStatus or PENDING if not found
     */
    public static PaymentStatus fromValue(String value) {
        if (value == null) return PENDING;
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PENDING;
    }
}
