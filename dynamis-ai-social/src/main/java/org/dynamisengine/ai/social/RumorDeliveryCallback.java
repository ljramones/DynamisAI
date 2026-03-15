package org.dynamisengine.ai.social;

import org.dynamisengine.core.entity.EntityId;

/**
 * Callback invoked when a Rumor is delivered to a recipient agent.
 * Implement to ingest rumors into the agent's belief model.
 */
@FunctionalInterface
public interface RumorDeliveryCallback {

    /**
     * Called after a rumor is successfully enqueued for a recipient.
     *
     * @param recipient the agent receiving the rumor
     * @param rumor the rumor as delivered (after hop/fidelity decay)
     * @param senderTrust trust score the recipient has for the sender (0..1)
     */
    void onDelivered(EntityId recipient, Rumor rumor, float senderTrust);
}
