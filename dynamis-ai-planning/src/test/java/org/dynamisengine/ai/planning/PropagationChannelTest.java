package org.dynamisengine.ai.planning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropagationChannelTest {

    @Test
    void enumHasThreeChannels() {
        assertEquals(3, PropagationChannel.values().length);
        assertEquals(PropagationChannel.RADIO, PropagationChannel.valueOf("RADIO"));
        assertEquals(PropagationChannel.LINE_OF_SIGHT, PropagationChannel.valueOf("LINE_OF_SIGHT"));
        assertEquals(PropagationChannel.RUNNER, PropagationChannel.valueOf("RUNNER"));
    }
}
