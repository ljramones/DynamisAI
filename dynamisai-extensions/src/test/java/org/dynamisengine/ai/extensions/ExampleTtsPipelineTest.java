package org.dynamisengine.ai.extensions;

import org.dynamisengine.ai.cognition.AffectVector;
import org.dynamisengine.ai.cognition.DialogueResponse;
import org.dynamisengine.core.entity.EntityId;
import org.dynamisengine.ai.testkit.TTSPipelineContractTest;
import org.dynamisengine.ai.voice.BarkType;
import org.dynamisengine.ai.voice.PhysicalVoiceContext;
import org.dynamisengine.ai.voice.TTSPipeline;
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
