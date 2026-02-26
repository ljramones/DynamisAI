package org.dynamisai.crowd;

import org.dynamisai.navigation.NavPoint;

/**
 * A computed position slot in a formation.
 */
public record FormationSlot(
    int slotIndex,
    NavPoint worldPosition,
    NavPoint facingDirection
) {}
