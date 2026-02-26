package org.dynamisai.planning;

import java.util.Objects;

public final class BehaviorTree {

    private final String name;
    private final BtNode root;
    private BtStatus lastStatus = null;

    public BehaviorTree(String name, BtNode root) {
        this.name = Objects.requireNonNull(name);
        this.root = Objects.requireNonNull(root);
    }

    public BtStatus tick(BtContext context) {
        if (lastStatus == BtStatus.SUCCESS || lastStatus == BtStatus.FAILURE) {
            root.reset();
        }
        BtStatus current = root.tick(context);
        lastStatus = current;
        return current;
    }

    public void reset() {
        root.reset();
        lastStatus = null;
    }

    public String name() {
        return name;
    }
}
