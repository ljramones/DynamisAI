package org.dynamisengine.ai.planning;

import java.util.List;

public final class BtSelector implements BtNode {

    private final List<BtNode> children;
    private int currentIndex = 0;

    public BtSelector(List<BtNode> children) {
        this.children = List.copyOf(children);
    }

    public BtSelector(BtNode... children) {
        this(List.of(children));
    }

    @Override
    public BtStatus tick(BtContext ctx) {
        while (currentIndex < children.size()) {
            BtNode child = children.get(currentIndex);
            BtStatus status = child.tick(ctx);
            if (status == BtStatus.SUCCESS) {
                return BtStatus.SUCCESS;
            }
            if (status == BtStatus.RUNNING) {
                return BtStatus.RUNNING;
            }
            currentIndex++;
        }
        return BtStatus.FAILURE;
    }

    @Override
    public void reset() {
        currentIndex = 0;
        for (BtNode child : children) {
            child.reset();
        }
    }
}
