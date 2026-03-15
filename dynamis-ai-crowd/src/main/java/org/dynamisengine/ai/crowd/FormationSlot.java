package org.dynamisengine.ai.crowd;

import org.dynamisengine.ai.navigation.NavPoint;

/**
 * A computed position slot in a formation.
 */
public record FormationSlot(
    int slotIndex,
    NavPoint worldPosition,
    NavPoint facingDirection
) {}
