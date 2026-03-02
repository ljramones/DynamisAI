package org.dynamisai.cognition;

import org.dynamis.core.entity.EntityId;
import org.dynamisscripting.api.value.CanonTime;
import org.dynamisai.core.DefaultWorldStateStore;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldFacts;
import org.dynamisai.core.WorldSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DefaultCognitionServiceStaleTickTest {

    private DialogueRequest request(EntityId speaker, WorldSnapshot snapshot) {
        return new DialogueRequest(
            speaker, EntityId.of(99L), "hello",
            new WorldFacts(Map.of(), List.of(),
                ThreatLevel.NONE, new Location(0, 0, 0), null),
            AffectVector.neutral(), snapshot
        );
    }

    @Test
    void resultWithinStaleWindowIsReturnedNormally() throws Exception {
        MockInferenceBackend backend = new MockInferenceBackend();
        backend.setSimulatedLatencyMs(120L);
        AtomicReference<CanonTime> now = new AtomicReference<>(CanonTime.of(100L, 0L));
        DefaultCognitionService service = new DefaultCognitionService(
            backend, new ResponseParser(), new ResponseCache(64), Map.of(), 500, now::get);

        try {
            var future = service.requestDialogue(
                request(EntityId.of(1L), new DefaultWorldStateStore().getCurrentSnapshot()));
            now.set(CanonTime.of(105L, 0L));

            DialogueResponse response = future.get(2, TimeUnit.SECONDS);
            assertNotEquals("...", response.text());
        } finally {
            service.shutdown();
        }
    }

    @Test
    void resultBeyondStaleWindowReturnsFallback() throws Exception {
        MockInferenceBackend backend = new MockInferenceBackend();
        backend.setSimulatedLatencyMs(120L);
        AtomicReference<CanonTime> now = new AtomicReference<>(CanonTime.of(10L, 0L));
        DefaultCognitionService service = new DefaultCognitionService(
            backend, new ResponseParser(), new ResponseCache(64), Map.of(), 500, now::get);

        try {
            var future = service.requestDialogue(
                request(EntityId.of(2L), new DefaultWorldStateStore().getCurrentSnapshot()));
            now.set(CanonTime.of(25L, 0L));

            DialogueResponse response = future.get(2, TimeUnit.SECONDS);
            assertEquals("...", response.text());
        } finally {
            service.shutdown();
        }
    }

    @Test
    void defaultCanonTimeZeroPreservesExistingBehavior() throws Exception {
        MockInferenceBackend backend = new MockInferenceBackend();
        backend.setSimulatedLatencyMs(120L);
        DefaultCognitionService service = new DefaultCognitionService(backend);

        try {
            DialogueResponse response = service.requestDialogue(
                    request(EntityId.of(3L), new DefaultWorldStateStore().getCurrentSnapshot()))
                .get(2, TimeUnit.SECONDS);
            assertNotEquals("...", response.text());
        } finally {
            service.shutdown();
        }
    }
}
