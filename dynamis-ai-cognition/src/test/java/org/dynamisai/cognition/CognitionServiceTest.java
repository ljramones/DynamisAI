package org.dynamisai.cognition;

import org.dynamisai.core.DefaultWorldStateStore;
import org.dynamisai.core.EntityId;
import org.dynamisai.core.Location;
import org.dynamisai.core.ThreatLevel;
import org.dynamisai.core.WorldFacts;
import org.dynamisai.core.WorldSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CognitionServiceTest {

    private MockInferenceBackend mockBackend;
    private DefaultCognitionService service;
    private WorldSnapshot stubSnapshot;

    @BeforeEach
    void setUp() {
        mockBackend = new MockInferenceBackend();
        service = new DefaultCognitionService(mockBackend);
        stubSnapshot = new DefaultWorldStateStore().getCurrentSnapshot();
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    private DialogueRequest request(EntityId speaker, String speech) {
        return new DialogueRequest(
            speaker, EntityId.of(99L), speech,
            new WorldFacts(Map.of(), List.of(),
                ThreatLevel.NONE, new Location(0, 0, 0), null),
            AffectVector.neutral(), stubSnapshot
        );
    }

    @Test
    void requestDialogueReturnsFuture() throws Exception {
        CompletableFuture<DialogueResponse> future =
            service.requestDialogue(request(EntityId.of(1L), "Hello"));
        DialogueResponse response = future.get(2, TimeUnit.SECONDS);
        assertNotNull(response);
        assertNotNull(response.text());
        assertFalse(response.text().isEmpty());
    }

    @Test
    void responseIsCachedAfterSuccessfulRequest() throws Exception {
        EntityId speaker = EntityId.of(2L);
        service.requestDialogue(request(speaker, "Test")).get(2, TimeUnit.SECONDS);
        DialogueResponse fallback = service.getFallback(speaker);
        assertTrue(fallback.fromCache());
    }

    @Test
    void fallbackServedWhenBackendFails() throws Exception {
        MockInferenceBackend failing = new MockInferenceBackend("{}", true);
        DefaultCognitionService failingService = new DefaultCognitionService(failing);
        try {
            EntityId speaker = EntityId.of(3L);
            DialogueResponse response =
                failingService.requestDialogue(request(speaker, "Hi"))
                    .get(2, TimeUnit.SECONDS);
            assertNotNull(response);
            assertEquals("...", response.text());
        } finally {
            failingService.shutdown();
        }
    }

    @Test
    void warmCachePrePopulatesResponse() {
        EntityId speaker = EntityId.of(4L);
        DialogueResponse preloaded = DialogueResponse.fallback("Preloaded line.");
        service.warmCache(speaker, preloaded);
        DialogueResponse fallback = service.getFallback(speaker);
        assertTrue(fallback.fromCache());
    }

    @Test
    void getFallbackWithAuthoredLineReturnsIt() {
        EntityId speaker = EntityId.of(5L);
        Map<EntityId, String> lines = Map.of(speaker, "Stand back, citizen.");
        DefaultCognitionService withLines = new DefaultCognitionService(
            mockBackend, new ResponseParser(), new ResponseCache(64), lines);
        try {
            DialogueResponse response = withLines.getFallback(speaker);
            assertEquals("Stand back, citizen.", response.text());
        } finally {
            withLines.shutdown();
        }
    }

    @Test
    void getFallbackGenericWhenNoCacheNoLine() {
        EntityId unknown = EntityId.of(999L);
        DialogueResponse response = service.getFallback(unknown);
        assertEquals("...", response.text());
    }

    @Test
    void multipleRequestsCompleteWithoutError() throws Exception {
        EntityId speaker = EntityId.of(6L);
        CompletableFuture<?>[] futures = new CompletableFuture[5];
        for (int i = 0; i < 5; i++) {
            futures[i] = service.requestDialogue(request(speaker, "message " + i));
        }
        CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);
    }

    @Test
    void backendCallCountMatchesRequests() throws Exception {
        service.requestDialogue(request(EntityId.of(7L), "one")).get(2, TimeUnit.SECONDS);
        service.requestDialogue(request(EntityId.of(8L), "two")).get(2, TimeUnit.SECONDS);
        assertEquals(2, mockBackend.getCallCount());
    }

    @Test
    void shutdownCompletesCleanly() {
        assertDoesNotThrow(() -> service.shutdown());
    }
}
