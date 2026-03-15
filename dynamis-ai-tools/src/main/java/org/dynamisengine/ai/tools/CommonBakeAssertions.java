package org.dynamisengine.ai.tools;

import org.dynamisengine.ai.core.AIOutputFrame;
import org.dynamisengine.ai.core.DialogueEvent;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.core.Location;
import org.dynamisengine.ai.core.SteeringOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CommonBakeAssertions {

    private CommonBakeAssertions() {}

    public static BakeAssertion maxSpeed(float maxSpeed) {
        return new BakeAssertion() {
            @Override
            public String name() {
                return "maxSpeed(" + maxSpeed + ")";
            }

            @Override
            public Optional<String> evaluate(long tick, AIOutputFrame frame) {
                if (frame == null || frame.steeringOutputs() == null) {
                    return Optional.empty();
                }
                for (Map.Entry<EntityId, SteeringOutput> e : frame.steeringOutputs().entrySet()) {
                    SteeringOutput output = e.getValue();
                    Location v = output == null ? null : output.desiredVelocity();
                    float mag = v == null ? 0f : magnitude(v);
                    if (mag > maxSpeed) {
                        return Optional.of("entity=" + e.getKey() + " speed=" + mag + " exceeds " + maxSpeed);
                    }
                }
                return Optional.empty();
            }
        };
    }

    public static BakeAssertion noBlankDialogue() {
        return new BakeAssertion() {
            @Override
            public String name() {
                return "noBlankDialogue";
            }

            @Override
            public Optional<String> evaluate(long tick, AIOutputFrame frame) {
                if (frame == null || frame.dialogueEvents() == null) {
                    return Optional.empty();
                }
                for (DialogueEvent event : frame.dialogueEvents()) {
                    if (event == null || event.text() == null || event.text().isBlank()) {
                        return Optional.of("blank dialogue at tick " + tick);
                    }
                }
                return Optional.empty();
            }
        };
    }

    public static BakeAssertion dialogueRateLimit(int maxPerTick) {
        return new BakeAssertion() {
            @Override
            public String name() {
                return "dialogueRateLimit(" + maxPerTick + ")";
            }

            @Override
            public Optional<String> evaluate(long tick, AIOutputFrame frame) {
                if (frame == null || frame.dialogueEvents() == null) {
                    return Optional.empty();
                }
                Map<EntityId, Integer> counts = new HashMap<>();
                for (DialogueEvent event : frame.dialogueEvents()) {
                    if (event == null || event.speaker() == null) {
                        continue;
                    }
                    int count = counts.getOrDefault(event.speaker(), 0) + 1;
                    counts.put(event.speaker(), count);
                    if (count > maxPerTick) {
                        return Optional.of("speaker " + event.speaker() + " produced " + count + " events");
                    }
                }
                return Optional.empty();
            }
        };
    }

    public static BakeAssertion steeringOutputNotNull() {
        return new BakeAssertion() {
            @Override
            public String name() {
                return "steeringOutputNotNull";
            }

            @Override
            public Optional<String> evaluate(long tick, AIOutputFrame frame) {
                if (frame == null) {
                    return Optional.of("frame is null");
                }
                return frame.steeringOutputs() == null
                    ? Optional.of("steeringOutputs is null")
                    : Optional.empty();
            }
        };
    }

    public static BakeAssertion noMassStall(float maxFraction, int stallTicks) {
        return new BakeAssertion() {
            private final Map<EntityId, Integer> consecutiveStalls = new HashMap<>();

            @Override
            public String name() {
                return "noMassStall(" + maxFraction + "," + stallTicks + ")";
            }

            @Override
            public Optional<String> evaluate(long tick, AIOutputFrame frame) {
                if (frame == null || frame.steeringOutputs() == null || frame.steeringOutputs().isEmpty()) {
                    consecutiveStalls.clear();
                    return Optional.empty();
                }

                for (Map.Entry<EntityId, SteeringOutput> e : frame.steeringOutputs().entrySet()) {
                    EntityId id = e.getKey();
                    SteeringOutput output = e.getValue();
                    boolean stalled = output == null || isZero(output.desiredVelocity());
                    if (stalled) {
                        consecutiveStalls.put(id, consecutiveStalls.getOrDefault(id, 0) + 1);
                    } else {
                        consecutiveStalls.put(id, 0);
                    }
                }

                int currentAgents = frame.steeringOutputs().size();
                int stalledAgents = 0;
                for (EntityId id : frame.steeringOutputs().keySet()) {
                    if (consecutiveStalls.getOrDefault(id, 0) >= stallTicks) {
                        stalledAgents++;
                    }
                }

                float fraction = currentAgents == 0 ? 0f : (float) stalledAgents / currentAgents;
                if (fraction > maxFraction) {
                    return Optional.of("mass stall fraction=" + fraction + " stalled=" + stalledAgents + "/" + currentAgents);
                }
                return Optional.empty();
            }
        };
    }

    private static float magnitude(Location v) {
        return (float) Math.sqrt(v.x() * v.x() + v.y() * v.y() + v.z() * v.z());
    }

    private static boolean isZero(Location v) {
        if (v == null) {
            return true;
        }
        return Math.abs(v.x()) < 1e-5f && Math.abs(v.y()) < 1e-5f && Math.abs(v.z()) < 1e-5f;
    }
}
