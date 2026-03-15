package org.dynamisengine.ai.planning;

public final class BtDecorator implements BtNode {

    public enum Type {
        INVERTER,
        SUCCEEDER,
        REPEATER,
        UNTIL_FAIL
    }

    private final Type type;
    private final BtNode child;
    private final int repeatCount;
    private int repeated;

    public BtDecorator(Type type, BtNode child) {
        this(type, 1, child);
    }

    public BtDecorator(Type type, int repeatCount, BtNode child) {
        this.type = type;
        this.child = child;
        this.repeatCount = repeatCount;
    }

    @Override
    public BtStatus tick(BtContext ctx) {
        BtStatus childStatus = child.tick(ctx);

        return switch (type) {
            case INVERTER -> {
                if (childStatus == BtStatus.SUCCESS) {
                    yield BtStatus.FAILURE;
                }
                if (childStatus == BtStatus.FAILURE) {
                    yield BtStatus.SUCCESS;
                }
                yield BtStatus.RUNNING;
            }
            case SUCCEEDER -> BtStatus.SUCCESS;
            case REPEATER -> {
                if (childStatus == BtStatus.RUNNING) {
                    yield BtStatus.RUNNING;
                }
                repeated++;
                if (repeated >= repeatCount) {
                    yield BtStatus.SUCCESS;
                }
                child.reset();
                yield BtStatus.RUNNING;
            }
            case UNTIL_FAIL -> {
                if (childStatus == BtStatus.FAILURE) {
                    yield BtStatus.FAILURE;
                }
                if (childStatus == BtStatus.RUNNING) {
                    yield BtStatus.RUNNING;
                }
                child.reset();
                yield BtStatus.RUNNING;
            }
        };
    }

    @Override
    public void reset() {
        repeated = 0;
        child.reset();
    }
}
