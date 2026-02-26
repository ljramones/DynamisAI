package org.dynamisai.core;

public interface BudgetGovernor {
    void register(AITaskNode node);
    void unregister(String taskId);
    void setLodPolicy(AILODPolicy policy);
    void runFrame(long tick, WorldSnapshot snapshot);
    FrameBudgetReport getLastFrameReport();
    int getRegisteredTaskCount();
}
