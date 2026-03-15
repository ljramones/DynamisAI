package org.dynamisengine.ai.planning;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GoapActionLibrary {

    private final Map<String, GoapAction> actions = new LinkedHashMap<>();

    public void register(GoapAction action) {
        actions.put(action.name(), action);
    }

    public void unregister(String actionName) {
        actions.remove(actionName);
    }

    public List<GoapAction> all() {
        return List.copyOf(actions.values());
    }

    public List<GoapAction> applicableTo(WorldState state) {
        List<GoapAction> applicable = new ArrayList<>();
        for (GoapAction action : actions.values()) {
            if (action.precondition().test(state)) {
                applicable.add(action);
            }
        }
        return applicable;
    }

    public Optional<GoapAction> find(String name) {
        return Optional.ofNullable(actions.get(name));
    }

    public int size() {
        return actions.size();
    }
}
