package org.dynamisengine.ai.planning;

import java.util.List;

public final class BtParallel implements BtNode {

    public enum Policy { REQUIRE_ALL, REQUIRE_ONE }

    private final Policy successPolicy;
    private final Policy failurePolicy;
    private final List<BtNode> children;

    public BtParallel(Policy successPolicy, Policy failurePolicy, List<BtNode> children) {
        this.successPolicy = successPolicy;
        this.failurePolicy = failurePolicy;
        this.children = List.copyOf(children);
    }

    @Override
    public BtStatus tick(BtContext ctx) {
        int successCount = 0;
        int failureCount = 0;

        for (BtNode child : children) {
            BtStatus status = child.tick(ctx);
            if (status == BtStatus.SUCCESS) {
                successCount++;
            } else if (status == BtStatus.FAILURE) {
                failureCount++;
            }
        }

        boolean success = successPolicy == Policy.REQUIRE_ALL
            ? successCount == children.size()
            : successCount >= 1;
        if (success) {
            return BtStatus.SUCCESS;
        }

        boolean failure = failurePolicy == Policy.REQUIRE_ALL
            ? failureCount == children.size()
            : failureCount >= 1;
        if (failure) {
            return BtStatus.FAILURE;
        }

        return BtStatus.RUNNING;
    }

    @Override
    public void reset() {
        for (BtNode child : children) {
            child.reset();
        }
    }
}
