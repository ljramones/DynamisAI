package org.dynamisai.cognition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseParserTest {

    private final ResponseParser parser = new ResponseParser();

    @Test
    void parsesWellFormedJson() {
        String json = "{\"text\":\"Hello there.\",\"affect\":{\"valence\":0.5," +
            "\"arousal\":0.4,\"dominance\":0.6,\"sarcasm\":0.0,\"intensity\":0.5}," +
            "\"tags\":[\"sigh\"],\"hints\":[]}";
        DialogueResponse r = parser.parse(json);
        assertEquals("Hello there.", r.text());
        assertEquals(0.5f, r.affect().valence(), 0.01f);
        assertTrue(r.nonverbalTags().contains("sigh"));
    }

    @Test
    void returnsNeutralAffectWhenAffectMissing() {
        String json = "{\"text\":\"Hi.\",\"tags\":[],\"hints\":[]}";
        DialogueResponse r = parser.parse(json);
        assertEquals(AffectVector.neutral(), r.affect());
    }

    @Test
    void returnsEllipsisForEmptyInput() {
        assertEquals("...", parser.parse("").text());
        assertEquals("...", parser.parse(null).text());
    }

    @Test
    void returnsEllipsisForMalformedJson() {
        assertEquals("...", parser.parse("{broken json {{").text());
    }

    @Test
    void clampsOutOfRangeAffectValues() {
        String json = "{\"text\":\"x\",\"affect\":{\"valence\":5.0," +
            "\"arousal\":-2.0,\"dominance\":0.5,\"sarcasm\":0.0,\"intensity\":0.5}," +
            "\"tags\":[],\"hints\":[]}";
        DialogueResponse r = parser.parse(json);
        assertEquals(1.0f, r.affect().valence(), 0.01f);
        assertEquals(0.0f, r.affect().arousal(), 0.01f);
    }

    @Test
    void parsesMultipleTags() {
        String json = "{\"text\":\"Hmm.\",\"affect\":{\"valence\":0.0,\"arousal\":0.3," +
            "\"dominance\":0.5,\"sarcasm\":0.0,\"intensity\":0.3}," +
            "\"tags\":[\"sigh\",\"whisper\"],\"hints\":[]}";
        DialogueResponse r = parser.parse(json);
        assertEquals(2, r.nonverbalTags().size());
        assertTrue(r.nonverbalTags().contains("whisper"));
    }
}
