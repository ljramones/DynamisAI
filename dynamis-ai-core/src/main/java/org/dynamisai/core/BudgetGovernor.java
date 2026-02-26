package org.dynamisai.core;

public interface BudgetGovernor {
    void register(AITaskNode node);
    void unregister(String taskId);
    void runFrame(long tick, WorldSnapshot snapshot);
    FrameBudgetReport getLastFrameReport();
    int getRegisteredTaskCount();
}
