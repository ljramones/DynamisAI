package org.dynamisengine.ai.testkit;

import org.dynamisengine.ai.memory.EmbeddingVector;
import org.dynamisengine.ai.memory.SentenceEncoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class SentenceEncoderContractTest {

    protected abstract SentenceEncoder createSubject();

    @Test
    void encoderProducesCorrectDim() {
        SentenceEncoder enc = createSubject();
        EmbeddingVector v = enc.encode("hello world");
        assertEquals(enc.dim(), v.dim());
    }

    @Test
    void encoderIsDeterministic() {
        SentenceEncoder enc = createSubject();
        EmbeddingVector a = enc.encode("the guard patrols the market");
        EmbeddingVector b = enc.encode("the guard patrols the market");
        assertTrue(a.approximatelyEquals(b, 1e-5f));
    }

    @Test
    void encoderNullReturnsVector() {
        SentenceEncoder enc = createSubject();
        EmbeddingVector v = enc.encode(null);
        assertEquals(enc.dim(), v.dim());
    }

    @Test
    void encoderDifferentTextsProduceDifferentVectors() {
        SentenceEncoder enc = createSubject();
        EmbeddingVector a = enc.encode("peaceful merchant selling bread");
        EmbeddingVector b = enc.encode("violent armed warrior attacking");
        assertFalse(a.approximatelyEquals(b, 1e-3f));
    }
}
