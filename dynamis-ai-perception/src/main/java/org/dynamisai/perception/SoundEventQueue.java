package org.dynamisai.perception;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Thread-safe event buffer for sound events consumed by SimulatedSenses. */
public final class SoundEventQueue {

    private final Object lock = new Object();
    private List<SoundEvent> buffer = new ArrayList<>();

    public void post(SoundEvent event) {
        Objects.requireNonNull(event);
        synchronized (lock) {
            buffer.add(event);
        }
    }

    /**
     * Drains all currently queued sound events atomically.
     * The tick argument is accepted for API clarity and future filtering.
     */
    public List<SoundEvent> drainForTick(long tick) {
        synchronized (lock) {
            List<SoundEvent> drained = List.copyOf(buffer);
            buffer = new ArrayList<>();
            return drained;
        }
    }

    public List<SoundEvent> peek() {
        synchronized (lock) {
            return List.copyOf(buffer);
        }
    }
}
