package org.dynamisai.social;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class DramaManager {

    private final DramaManagerConfig config;
    private final ConcurrentLinkedQueue<BehaviorModifierEvent> eventQueue = new ConcurrentLinkedQueue<>();

    private DramaticBeat currentBeat = DramaticBeat.LULL;
    private long lastBeatChangeTick = Long.MIN_VALUE;

    public DramaManager(DramaManagerConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    public List<BehaviorModifierEvent> evaluate(EngagementMetrics metrics, long currentTick) {
        Objects.requireNonNull(metrics);

        DramaticBeat target = determineBeat(metrics);
        if (target != currentBeat && canTransition(currentTick)) {
            currentBeat = target;
            lastBeatChangeTick = currentTick;
            emitForBeat(currentBeat, currentTick);
        }

        return List.copyOf(eventQueue);
    }

    public DramaticBeat currentBeat() {
        return currentBeat;
    }

    public void forceBeat(DramaticBeat beat, long currentTick) {
        if (beat == null) {
            return;
        }
        currentBeat = beat;
        lastBeatChangeTick = currentTick;
        emitForBeat(beat, currentTick);
    }

    public List<BehaviorModifierEvent> drainEvents() {
        List<BehaviorModifierEvent> out = new ArrayList<>();
        BehaviorModifierEvent e;
        while ((e = eventQueue.poll()) != null) {
            out.add(e);
        }
        return List.copyOf(out);
    }

    private DramaticBeat determineBeat(EngagementMetrics metrics) {
        if (metrics.combatIntensity() > config.escalationThreshold()) {
            return DramaticBeat.ESCALATION;
        }

        if (currentBeat == DramaticBeat.ESCALATION
            && metrics.combatIntensity() < config.reliefThreshold()) {
            return DramaticBeat.RELIEF;
        }

        if (metrics.threatPressure() > config.tensionThreshold()) {
            return DramaticBeat.TENSION_BUILDING;
        }

        if (metrics.combatIntensity() < config.lullThreshold()
            && metrics.explorationRate() < config.lullThreshold()
            && metrics.decisionSpeed() < config.lullThreshold()
            && metrics.threatPressure() < config.lullThreshold()) {
            return DramaticBeat.LULL;
        }

        if (metrics.idleTime() > 0.5f) {
            return DramaticBeat.REVELATION;
        }

        return currentBeat;
    }

    private boolean canTransition(long tick) {
        if (lastBeatChangeTick == Long.MIN_VALUE) {
            return true;
        }
        return (tick - lastBeatChangeTick) >= config.minTicksBetweenBeatChanges();
    }

    private void emitForBeat(DramaticBeat beat, long tick) {
        switch (beat) {
            case TENSION_BUILDING -> {
                emit(BehaviorModifierType.FACTION_AGGRESSION, 0.5f, null,
                    "faction.aggression", tick);
                emit(BehaviorModifierType.ENCOUNTER_DENSITY, 0.4f, null,
                    "encounter.density", tick);
            }
            case ESCALATION -> {
                emit(BehaviorModifierType.FACTION_AGGRESSION,
                    config.maxAggressionMagnitude(), null,
                    "faction.aggression", tick);
                emit(BehaviorModifierType.DIFFICULTY_OFFSET, 0.3f, null,
                    "difficulty.offset", tick);
            }
            case RELIEF -> {
                emit(BehaviorModifierType.RESOURCE_AVAILABILITY, 0.7f, null,
                    "resource.availability", tick);
                emit(BehaviorModifierType.FACTION_AGGRESSION, 0.1f, null,
                    "faction.aggression", tick);
            }
            case REVELATION -> emit(BehaviorModifierType.AMBIENT_DENSITY, 0.6f, null,
                "ambient.density", tick);
            case LULL -> {
                emit(BehaviorModifierType.AMBIENT_DENSITY, 0.3f, null,
                    "ambient.density", tick);
                emit(BehaviorModifierType.FACTION_AGGRESSION, 0f, null,
                    "faction.aggression", tick);
            }
        }
    }

    private void emit(BehaviorModifierType type,
                      float magnitude,
                      String targetFactionId,
                      String parameter,
                      long tick) {
        eventQueue.add(new BehaviorModifierEvent(type, magnitude, targetFactionId, parameter, tick));
    }
}
