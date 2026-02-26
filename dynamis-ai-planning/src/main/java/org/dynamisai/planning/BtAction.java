package org.dynamisai.planning;

import java.util.Objects;
import java.util.function.Function;

public final class BtAction implements BtNode {

    private final String name;
    private final Function<BtContext, BtStatus> action;
    private boolean running;

    public BtAction(String name, Function<BtContext, BtStatus> action) {
        this.name = Objects.requireNonNull(name);
        this.action = Objects.requireNonNull(action);
    }

    public String name() {
        return name;
    }

    @Override
    public BtStatus tick(BtContext ctx) {
        BtStatus status = action.apply(ctx);
        running = status == BtStatus.RUNNING;
        return status;
    }

    @Override
    public void reset() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
