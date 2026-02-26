package org.dynamisai.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleNpcDemoReelTest {

    private static final String[] PLAYER_INPUTS = {
        "What's happening in the market today?",
        "Have you seen any guards nearby?",
        "I need to find the blacksmith.",
        "Is the road to Eastgate safe?",
        "What do you know about the thieves' guild?"
    };

    @Test
    void demoReelRunsTenTicksWithoutError() throws Exception {
        SingleNpcDemoReel reel = new SingleNpcDemoReel(10, PLAYER_INPUTS);
        DemoReelReport report = reel.run();

        assertNotNull(report);
        assertEquals(10, report.totalTicks());
        assertTrue(report.allTicksSucceeded(),
            "All 10 ticks must produce a result");
    }

    @Test
    void allTicksProduceDialogue() throws Exception {
        SingleNpcDemoReel reel = new SingleNpcDemoReel(10, PLAYER_INPUTS);
        DemoReelReport report = reel.run();

        for (DemoTickResult result : report.tickResults()) {
            assertNotNull(result.dialogueText(),
                "Tick " + result.tick() + " must produce non-null dialogue");
            assertFalse(result.dialogueText().isEmpty(),
                "Tick " + result.tick() + " dialogue must not be empty");
        }
    }

    @Test
    void memoryGrowsAcrossTicks() throws Exception {
        SingleNpcDemoReel reel = new SingleNpcDemoReel(10, PLAYER_INPUTS);
        DemoReelReport report = reel.run();

        assertEquals(10, report.totalMemoryEvents(),
            "One memory event must be recorded per tick");
        assertTrue(report.tickResults().get(9).memoryStats().totalCount() > 0,
            "Memory must contain records after 10 ticks");
    }

    @Test
    void npcPerceivedPlayerAtLeastOnce() throws Exception {
        SingleNpcDemoReel reel = new SingleNpcDemoReel(10, PLAYER_INPUTS);
        DemoReelReport report = reel.run();

        assertTrue(report.totalPercepts() > 0,
            "NPC must perceive the player at least once across 10 ticks");
    }

    @Test
    void reportJsonIsWellFormed() throws Exception {
        SingleNpcDemoReel reel = new SingleNpcDemoReel(5, PLAYER_INPUTS);
        DemoReelReport report = reel.run();
        String json = report.toJson();

        assertNotNull(json);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertTrue(json.contains("\"totalTicks\""));
        assertTrue(json.contains("\"tickResults\""));
    }

    @Test
    void frameBudgetIsReasonable() throws Exception {
        SingleNpcDemoReel reel = new SingleNpcDemoReel(10, PLAYER_INPUTS);
        DemoReelReport report = reel.run();

        for (DemoTickResult result : report.tickResults()) {
            assertTrue(result.frameBudgetMs() < 5000,
                "Frame at tick " + result.tick() +
                " took suspiciously long: " + result.frameBudgetMs() + "ms");
        }
    }
}
