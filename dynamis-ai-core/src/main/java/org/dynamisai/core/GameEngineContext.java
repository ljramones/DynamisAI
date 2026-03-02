package org.dynamisai.core;

import org.dynamis.core.entity.EntityId;
import java.util.List;
import java.util.Map;

/**
 * One-tick context payload provided by a game engine.
 */
public record GameEngineContext(
    long tick,
    float deltaTime,
    Location observerPosition,
    Map<EntityId, Location> entityPositions,
    List<WorldChange> worldChanges,
    PlayerInput playerInput
) {
    public static Builder builder(long tick, float deltaTime) {
        return new Builder(tick, deltaTime);
    }

    public static final class Builder {
        private final long tick;
        private final float deltaTime;
        private Location observerPosition = new Location(0, 0, 0);
        private Map<EntityId, Location> entityPositions = Map.of();
        private List<WorldChange> worldChanges = List.of();
        private PlayerInput playerInput;

        private Builder(long tick, float deltaTime) {
            this.tick = tick;
            this.deltaTime = deltaTime;
        }

        public Builder observer(Location pos) {
            this.observerPosition = pos;
            return this;
        }

        public Builder positions(Map<EntityId, Location> positions) {
            this.entityPositions = Map.copyOf(positions);
            return this;
        }

        public Builder changes(List<WorldChange> changes) {
            this.worldChanges = List.copyOf(changes);
            return this;
        }

        public Builder playerInput(PlayerInput input) {
            this.playerInput = input;
            return this;
        }

        public GameEngineContext build() {
            return new GameEngineContext(
                tick, deltaTime, observerPosition, entityPositions, worldChanges, playerInput);
        }
    }
}
