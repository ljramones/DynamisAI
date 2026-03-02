package org.dynamisai.cognition;

import org.dynamis.core.entity.EntityId;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class BeliefModel {

    private final EntityId owner;
    private final BeliefDecayPolicy decayPolicy;
    private final ConcurrentMap<String, Belief> firstOrder = new ConcurrentHashMap<>();
    private final ConcurrentMap<EntityId, ConcurrentMap<String, SecondOrderBelief>> secondOrder =
        new ConcurrentHashMap<>();

    public BeliefModel(EntityId owner, BeliefDecayPolicy decayPolicy) {
        this.owner = Objects.requireNonNull(owner);
        this.decayPolicy = Objects.requireNonNull(decayPolicy);
    }

    public void assertBelief(String key, Object value, float confidence, long tick) {
        firstOrder.compute(key, (k, existing) -> {
            if (existing == null) {
                return new Belief(k, value, clamp01(confidence), tick, tick, owner);
            }
            return new Belief(k, value, clamp01(confidence), existing.formedAtTick(), tick, owner);
        });
    }

    public Optional<Belief> getBelief(String key) {
        Belief belief = firstOrder.get(key);
        if (belief == null || belief.confidence() < decayPolicy.minimumRetainedConfidence()) {
            return Optional.empty();
        }
        return Optional.of(belief);
    }

    public List<Belief> allBeliefs() {
        return firstOrder.values().stream()
            .filter(b -> b.confidence() >= decayPolicy.minimumRetainedConfidence())
            .toList();
    }

    public List<Belief> staleBeliefs(long currentTick) {
        return allBeliefs().stream()
            .filter(b -> b.isStale(currentTick, decayPolicy.stalenessThresholdTicks()))
            .toList();
    }

    public void assertSecondOrder(EntityId subject, String key,
                                  float estimatedConfidence, long tick) {
        secondOrder.computeIfAbsent(subject, ignored -> new ConcurrentHashMap<>())
            .put(key, new SecondOrderBelief(owner, subject, key, clamp01(estimatedConfidence), tick));
    }

    public Optional<SecondOrderBelief> getSecondOrder(EntityId subject, String key) {
        Map<String, SecondOrderBelief> map = secondOrder.get(subject);
        if (map == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(map.get(key));
    }

    public void decay(long currentTick) {
        List<String> toRemove = new ArrayList<>();
        firstOrder.forEach((key, belief) -> {
            if (belief.lastReinforcedAtTick() < currentTick) {
                Belief decayed = belief.decayed(decayPolicy.decayPerTick());
                if (decayed.confidence() < decayPolicy.minimumRetainedConfidence()) {
                    toRemove.add(key);
                } else {
                    firstOrder.put(key, decayed);
                }
            }
        });
        toRemove.forEach(firstOrder::remove);
    }

    /**
     * Reflection bridge to avoid a module cycle (perception already requires cognition).
     * Expected shape: snapshot.percepts() -> List of objects with source(), stimulusType(),
     * location(), rawIntensity().
     */
    public void updateFromPerception(Object snapshot, long tick) {
        if (snapshot == null) {
            return;
        }
        try {
            Method perceptsMethod = snapshot.getClass().getMethod("percepts");
            Object perceptsObj = perceptsMethod.invoke(snapshot);
            if (!(perceptsObj instanceof List<?> percepts)) {
                return;
            }
            for (Object percept : percepts) {
                if (percept == null) {
                    continue;
                }
                EntityId source = invokeEntityId(percept, "source");
                if (source == null) {
                    continue;
                }
                Object location = invoke(percept, "location");
                float intensity = invokeFloat(percept, "rawIntensity");
                String stimulus = String.valueOf(invoke(percept, "stimulusType"));

                if ("VISUAL".equals(stimulus)) {
                    assertBelief("entity." + source.id() + ".visible", location, intensity, tick);
                } else if ("AUDITORY".equals(stimulus)) {
                    assertBelief("entity." + source.id() + ".audible", location, intensity, tick);
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Ignore incompatible snapshot objects.
        }
    }

    public EntityId owner() {
        return owner;
    }

    public int beliefCount() {
        return allBeliefs().size();
    }

    private static Object invoke(Object target, String method) throws ReflectiveOperationException {
        Method m = target.getClass().getMethod(method);
        return m.invoke(target);
    }

    private static EntityId invokeEntityId(Object target, String method)
        throws ReflectiveOperationException {
        Object result = invoke(target, method);
        return result instanceof EntityId id ? id : null;
    }

    private static float invokeFloat(Object target, String method) throws ReflectiveOperationException {
        Object result = invoke(target, method);
        if (result instanceof Number number) {
            return number.floatValue();
        }
        return 0f;
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
