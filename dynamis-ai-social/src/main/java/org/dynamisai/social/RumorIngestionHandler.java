package org.dynamisai.social;

import org.dynamis.core.entity.EntityId;
import org.dynamis.core.logging.DynamisLogger;
import org.dynamisai.cognition.BeliefModel;
import org.dynamisai.cognition.BeliefModelRegistry;
import org.dynamisai.core.BeliefSource;

import java.util.Objects;

/**
 * Ingests delivered Rumors into the recipient agent's BeliefModel.
 *
 * <p>Implements RumorDeliveryCallback - wire to RumorPropagator via
 * setDeliveryCallback() during runtime assembly.
 *
 * <p>Confidence = rumor.fidelity() * senderTrust, clamped to [0, 1].
 * Source is always BeliefSource.RUMOR - never PERCEPT.
 */
public final class RumorIngestionHandler implements RumorDeliveryCallback {

    private static final DynamisLogger log = DynamisLogger.get(RumorIngestionHandler.class);

    private final BeliefModelRegistry beliefRegistry;

    public RumorIngestionHandler(BeliefModelRegistry beliefRegistry) {
        this.beliefRegistry = Objects.requireNonNull(beliefRegistry);
    }

    @Override
    public void onDelivered(EntityId recipient, Rumor rumor, float senderTrust) {
        float confidence = Math.max(0f, Math.min(1f, rumor.fidelity() * senderTrust));
        if (confidence <= 0f) {
            log.debug(String.format(
                "Rumor %s discarded for %s - zero effective confidence (fidelity=%.2f trust=%.2f)",
                rumor.id(), recipient, rumor.fidelity(), senderTrust));
            return;
        }

        BeliefModel model = beliefRegistry.getOrCreate(recipient);
        String key = "rumor." + rumor.sourceEvent().type().name().toLowerCase()
            + ".actor." + rumor.sourceEvent().actor().id();

        model.updateBelief(
            key,
            rumor.sourceEvent(),
            confidence,
            rumor.propagatedAtTick(),
            BeliefSource.RUMOR);

        log.debug(String.format(
            "Rumor %s ingested for %s - key=%s confidence=%.2f",
            rumor.id(), recipient, key, confidence));
    }
}
