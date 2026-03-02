package org.dynamisai.extensions;

import org.dynamisai.cognition.AffectVector;
import org.dynamisai.cognition.DialogueResponse;
import org.dynamis.core.entity.EntityId;
import org.dynamisai.testkit.TTSPipelineContractTest;
import org.dynamisai.voice.BarkType;
import org.dynamisai.voice.PhysicalVoiceContext;
import org.dynamisai.voice.TTSPipeline;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExampleTtsPipelineTest extends TTSPipelineContractTest {

    @Override
    protected TTSPipeline createSubject() {
        return new ExampleTtsPipeline();
    }

    @Test
    void methodsCanBeCalledDirectly() {
        ExampleTtsPipeline pipeline = new ExampleTtsPipeline();
        DialogueResponse response = new DialogueResponse("hello", AffectVector.neutral(), List.of(), List.of(), false);
        assertTrue(pipeline.isAvailable());
        assertDoesNotThrow(() -> pipeline.render(response, PhysicalVoiceContext.calm(), EntityId.of(1L)).get());
        assertNotNull(pipeline.getFallbackBark(EntityId.of(1L), BarkType.ALERT));
    }
}
