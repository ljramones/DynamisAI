package org.dynamisengine.ai.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BakeViolationTest {

    @Test
    void recordConstructionAndAccess() {
        BakeViolation v = new BakeViolation("a", 10L, "msg", "frame");
        assertEquals("a", v.assertionName());
        assertEquals(10L, v.tick());
        assertEquals("msg", v.message());
        assertEquals("frame", v.frameDetails());
    }
}
