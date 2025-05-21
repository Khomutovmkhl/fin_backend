package com.example.fin.filtering;

/**
 * The enum Rest filter operator.
 */
public enum RestFilterOperator {
    /**
     * Equals operator
     * <p>
     * Single attribute: values match exactly
     * <p>
     * List: contains any of specified values
     */
    EQ,
    /**
     * Not equals where values do not match.
     */
    NEQ,
    /**
     * Where the value matches the specified pattern.
     */
    LIKE,
    /**
     * Lower than the specified value.
     */
    LT,
    /**
     * Lower than or equals the specified value.
     */
    LE,
    /**
     * Greater than the specified value.
     */
    GT,
    /**
     * Greater than or equals the specified value.
     */
    GE,
    /**
     * Contains any of specified values. Simply the same as EQ, but FE is using it, so we can remove it after FE removes it
     */
    CONTAINS_ANY,
    /**
     * Contains all of specified values.
     */
    CONTAINS_ALL,
    /**
     * Empty list of relationships.
     */
    EMPTY
}
