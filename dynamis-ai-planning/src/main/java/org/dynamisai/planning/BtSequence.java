package org.dynamisai.planning;

import java.util.List;

public final class BtSequence implements BtNode {

    private final List<BtNode> children;
    private int currentIndex = 0;

    public BtSequence(List<BtNode> children) {
        this.children = List.copyOf(children);
    }

    public BtSequence(BtNode... children) {
        this(List.of(children));
    }

    @Override
    public BtStatus tick(BtContext ctx) {
        while (currentIndex < children.size()) {
            BtNode child = children.get(currentIndex);
            BtStatus status = child.tick(ctx);
            if (status == BtStatus.SUCCESS) {
                currentIndex++;
                continue;
            }
            if (status == BtStatus.FAILURE) {
                return BtStatus.FAILURE;
            }
            return BtStatus.RUNNING;
        }
        return BtStatus.SUCCESS;
    }

    @Override
    public void reset() {
        currentIndex = 0;
        for (BtNode child : children) {
            child.reset();
        }
    }
}
