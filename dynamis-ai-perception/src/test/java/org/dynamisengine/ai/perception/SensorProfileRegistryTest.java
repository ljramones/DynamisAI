package org.dynamisengine.ai.perception;

import org.dynamisengine.core.entity.EntityId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorProfileRegistryTest {

    @Test
    void registerAndRetrieve() {
        SensorProfileRegistry registry = new SensorProfileRegistry();
        EntityId id = EntityId.of(1L);
        SensorProfile profile = new SensorProfile(10f, 90f, 5f, 0.2f, 45f);

        registry.register(id, profile);
        assertEquals(profile, registry.getOrDefault(id));
    }

    @Test
    void getOrDefaultNeverNull() {
        SensorProfileRegistry registry = new SensorProfileRegistry();
        SensorProfile profile = registry.getOrDefault(EntityId.of(999L));
        assertEquals(SensorProfile.defaultHuman(), profile);
    }

    @Test
    void unregisterRestoresDefault() {
        SensorProfileRegistry registry = new SensorProfileRegistry();
        EntityId id = EntityId.of(2L);
        registry.register(id, SensorProfile.blind());
        registry.unregister(id);
        assertEquals(SensorProfile.defaultHuman(), registry.getOrDefault(id));
        assertFalse(registry.isRegistered(id));
    }

    @Test
    void concurrentRegisterAndGetHasNoExceptions() {
        SensorProfileRegistry registry = new SensorProfileRegistry();
        ExecutorService exec = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                final int n = i;
                futures.add(exec.submit(() -> {
                    EntityId id = EntityId.of(n);
                    registry.register(id, SensorProfile.defaultHuman());
                    registry.getOrDefault(id);
                }));
            }
            for (Future<?> future : futures) {
                assertDoesNotThrow(() -> future.get());
            }
            assertTrue(registry.isRegistered(EntityId.of(50L)));
        } finally {
            exec.shutdownNow();
        }
    }
}
