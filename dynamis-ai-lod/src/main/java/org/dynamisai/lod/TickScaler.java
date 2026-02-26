package org.dynamisai.lod;

import org.dynamisai.core.LodTier;

public final class TickScaler {

    private TickScaler() {
    }

    public static double scaleRate(double baseHz, LodTier tier) {
        return switch (tier) {
            case TIER_0 -> baseHz;
            case TIER_1 -> baseHz / 2.0;
            case TIER_2 -> baseHz / 4.0;
            case TIER_3 -> baseHz / 16.0;
        };
    }

    public static boolean shouldTick(LodTier tier, long currentTick) {
        int divisor = switch (tier) {
            case TIER_0 -> 1;
            case TIER_1 -> 2;
            case TIER_2 -> 4;
            case TIER_3 -> 16;
        };
        return currentTick % divisor == 0;
    }
}
