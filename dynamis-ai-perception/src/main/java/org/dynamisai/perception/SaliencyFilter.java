package org.dynamisai.perception;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.WorldFacts;

/**
 * Filters raw WorldFacts into a prioritised Percept list.
 * Affect modulates attention: fear raises threat salience, curiosity raises novelty.
 */
public interface SaliencyFilter {

    /**
     * Apply affect-modulated saliency filtering to raw world facts.
     *
     * @param owner The perceiving NPC
     * @param facts Scoped world facts from WorldStateStore
     * @param affect Current emotional state — modulates what gets noticed
     * @param tick Current simulation tick
     * @return Ordered PerceptionSnapshot, highest salience first
     */
    PerceptionSnapshot filter(EntityId owner, WorldFacts facts,
                              AffectVector affect, long tick);
}
