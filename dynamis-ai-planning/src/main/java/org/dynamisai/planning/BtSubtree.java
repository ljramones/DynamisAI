package org.dynamisai.planning;

import java.util.Objects;

public final class BtSubtree implements BtNode {

    private final String name;
    private final BtNode root;

    public BtSubtree(String name, BtNode root) {
        this.name = Objects.requireNonNull(name);
        this.root = Objects.requireNonNull(root);
    }

    public String name() {
        return name;
    }

    @Override
    public BtStatus tick(BtContext ctx) {
        return root.tick(ctx);
    }

    @Override
    public void reset() {
        root.reset();
    }
}
