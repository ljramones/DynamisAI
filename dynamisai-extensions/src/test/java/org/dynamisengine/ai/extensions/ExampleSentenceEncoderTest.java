package org.dynamisengine.ai.extensions;

import org.dynamisengine.ai.memory.SentenceEncoder;
import org.dynamisengine.ai.testkit.SentenceEncoderContractTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExampleSentenceEncoderTest extends SentenceEncoderContractTest {

    @Override
    protected SentenceEncoder createSubject() {
        return new ExampleSentenceEncoder();
    }

    @Test
    void methodsCanBeCalledDirectly() {
        ExampleSentenceEncoder encoder = new ExampleSentenceEncoder();
        assertEquals(384, encoder.dim());
        assertEquals(384, encoder.encode("hello").dim());
        assertEquals(384, encoder.encode(null).dim());
    }
}
