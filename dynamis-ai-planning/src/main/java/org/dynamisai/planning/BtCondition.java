package org.dynamisai.planning;

import java.util.Objects;
import java.util.function.Predicate;

public final class BtCondition implements BtNode {

    private final String name;
    private final Predicate<BtContext> condition;

    public BtCondition(String name, Predicate<BtContext> condition) {
        this.name = Objects.requireNonNull(name);
        this.condition = Objects.requireNonNull(condition);
    }

    public String name() {
        return name;
    }

    @Override
    public BtStatus tick(BtContext ctx) {
        return condition.test(ctx) ? BtStatus.SUCCESS : BtStatus.FAILURE;
    }

    @Override
    public void reset() {
    }
}
