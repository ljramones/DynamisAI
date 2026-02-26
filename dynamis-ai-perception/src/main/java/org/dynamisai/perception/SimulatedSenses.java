package org.dynamisai.perception;

import io.vavr.collection.HashMap;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.EntityState;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Approximate vision/hearing model over snapshot entity positions and queued sounds. */
public final class SimulatedSenses {

    private final SensorProfileRegistry profileRegistry;
    private final SoundEventQueue soundEventQueue;

    public SimulatedSenses(SensorProfileRegistry profileRegistry,
                           SoundEventQueue soundEventQueue) {
        this.profileRegistry = Objects.requireNonNull(profileRegistry);
        this.soundEventQueue = Objects.requireNonNull(soundEventQueue);
    }

    public PerceptionSnapshot sense(EntityId observer, WorldSnapshot snapshot) {
        Objects.requireNonNull(observer);
        Objects.requireNonNull(snapshot);

        EntityState observerState = snapshot.entities().get(observer).getOrNull();
        if (observerState == null) {
            return PerceptionSnapshot.empty(observer, snapshot.tick(), new Location(0f, 0f, 0f));
        }

        SensorProfile profile = profileRegistry.getOrDefault(observer);
        Location observerPos = observerState.position();
        List<Percept> percepts = new ArrayList<>();

        // Visual pass: distance + field-of-view check (no occlusion/raycast yet).
        for (Map.Entry<EntityId, EntityState> entry : toJavaMap(snapshot.entities()).entrySet()) {
            EntityId targetId = entry.getKey();
            if (targetId.equals(observer)) {
                continue;
            }
            EntityState targetState = entry.getValue();
            if (isVisible(observerPos, targetState.position(), profile)) {
                float intensity = 1f -
                    (observerPos.distanceTo(targetState.position()) / profile.visionRadiusMetres());
                float clampedIntensity = clamp01(intensity);
                ThreatLevel threat = threatOf(targetState);
                percepts.add(new Percept(
                    targetId,
                    StimulusType.VISUAL,
                    targetState.position(),
                    clampedIntensity,
                    clampedIntensity,
                    threat,
                    false));
            }
        }

        for (SoundEvent sound : soundEventQueue.drainForTick(snapshot.tick())) {
            if (isAudible(observerPos, sound, profile)) {
                float distance = observerPos.distanceTo(sound.position());
                float effectiveIntensity = sound.intensity() *
                    (1f - (distance / profile.hearingRadiusMetres()));
                float clamped = clamp01(effectiveIntensity);
                percepts.add(new Percept(
                    sound.sourceEntityId(),
                    sound.type(),
                    sound.position(),
                    clamped,
                    clamped,
                    ThreatLevel.NONE,
                    false));
            }
        }

        return new PerceptionSnapshot(
            observer,
            snapshot.tick(),
            List.copyOf(percepts),
            percepts.stream().max((a, b) -> Float.compare(a.salienceScore(), b.salienceScore())),
            maxThreat(percepts),
            observerPos,
            percepts.size());
    }

    private static boolean isVisible(Location observer, Location target, SensorProfile profile) {
        if (profile.visionRadiusMetres() <= 0f || profile.visionAngleDegrees() <= 0f) {
            return false;
        }
        float distance = observer.distanceTo(target);
        if (distance > profile.visionRadiusMetres()) {
            return false;
        }
        float bearing = observer.bearingTo(target);
        float delta = angularDiffDegrees(profile.facingAngleDegrees(), bearing);
        return delta <= (profile.visionAngleDegrees() * 0.5f);
    }

    private static boolean isAudible(Location observer, SoundEvent sound, SensorProfile profile) {
        if (profile.hearingRadiusMetres() <= 0f) {
            return false;
        }
        float distance = observer.distanceTo(sound.position());
        if (distance > profile.hearingRadiusMetres()) {
            return false;
        }
        float effectiveIntensity = sound.intensity() *
            (1f - (distance / profile.hearingRadiusMetres()));
        return effectiveIntensity >= profile.hearingAcuity();
    }

    private static float angularDiffDegrees(float a, float b) {
        float raw = Math.abs(a - b) % 360f;
        return raw > 180f ? 360f - raw : raw;
    }

    private static float clamp01(float v) {
        if (v < 0f) {
            return 0f;
        }
        if (v > 1f) {
            return 1f;
        }
        return v;
    }

    @SuppressWarnings("unchecked")
    private static Map<EntityId, EntityState> toJavaMap(HashMap<EntityId, EntityState> entities) {
        return entities.toJavaMap();
    }

    private static ThreatLevel maxThreat(List<Percept> percepts) {
        ThreatLevel max = ThreatLevel.NONE;
        for (Percept percept : percepts) {
            if (percept.perceivedThreat().ordinal() > max.ordinal()) {
                max = percept.perceivedThreat();
            }
        }
        return max;
    }

    private static ThreatLevel threatOf(EntityState entityState) {
        Object value = entityState.properties().get("threatLevel");
        if (value instanceof ThreatLevel threatLevel) {
            return threatLevel;
        }
        return ThreatLevel.NONE;
    }
}
