package org.dynamisai.voice;

/**
 * Real-time physical state that modulates voice rendering.
 * Injected by the physics component — not authored by writers.
 */
public record PhysicalVoiceContext(
    boolean isRunning,
    boolean injured,
    boolean helmeted,
    boolean underwater,
    float distanceToListener,
    float exertionLevel
) {
    public PhysicalVoiceContext {
        if (distanceToListener < 0f) {
            throw new IllegalArgumentException("distanceToListener must be >= 0");
        }
        if (exertionLevel < 0f || exertionLevel > 1f) {
            throw new IllegalArgumentException("exertionLevel must be in [0,1]");
        }
    }

    public static PhysicalVoiceContext calm() {
        return new PhysicalVoiceContext(false, false, false, false, 3f, 0f);
    }

    public static PhysicalVoiceContext sprinting() {
        return new PhysicalVoiceContext(true, false, false, false, 3f, 0.9f);
    }

    public static PhysicalVoiceContext injuredState() {
        return new PhysicalVoiceContext(false, true, false, false, 3f, 0.3f);
    }
}
