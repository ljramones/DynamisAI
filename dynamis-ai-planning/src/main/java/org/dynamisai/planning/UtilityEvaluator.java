package org.dynamisai.planning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class UtilityEvaluator {

    private final Map<String, UtilityAction> actions = new LinkedHashMap<>();

    public void register(UtilityAction action) {
        actions.put(action.name(), action);
    }

    public void unregister(String name) {
        actions.remove(name);
    }

    public List<ScoredAction> evaluate(WorldState state) {
        List<ScoredAction> scored = new ArrayList<>();
        for (UtilityAction action : actions.values()) {
            scored.add(new ScoredAction(action, action.score(state)));
        }
        scored.sort(Comparator.comparingDouble(ScoredAction::score).reversed());
        return scored;
    }

    public Optional<UtilityAction> selectAndExecute(WorldState state) {
        List<ScoredAction> scored = evaluate(state);
        if (scored.isEmpty()) {
            return Optional.empty();
        }
        UtilityAction winner = scored.get(0).action();
        if (winner.onSelected() != null) {
            winner.onSelected().run();
        }
        return Optional.of(winner);
    }

    public int actionCount() {
        return actions.size();
    }
}
