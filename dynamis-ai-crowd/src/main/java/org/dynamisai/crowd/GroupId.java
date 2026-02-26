package org.dynamisai.crowd;

/**
 * Strongly-typed crowd group identifier.
 */
public record GroupId(long value) {

    private static final java.util.concurrent.atomic.AtomicLong SEQ =
        new java.util.concurrent.atomic.AtomicLong(1L);

    public static GroupId next() { return new GroupId(SEQ.getAndIncrement()); }
    public static GroupId of(long value) { return new GroupId(value); }

    @Override public String toString() { return "Group[" + value + "]"; }
}
