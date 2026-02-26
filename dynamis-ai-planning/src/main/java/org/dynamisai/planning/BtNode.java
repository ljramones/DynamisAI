package org.dynamisai.planning;

public sealed interface BtNode
    permits BtSequence, BtSelector, BtParallel,
            BtCondition, BtAction, BtDecorator, BtSubtree {

    BtStatus tick(BtContext context);

    void reset();
}
