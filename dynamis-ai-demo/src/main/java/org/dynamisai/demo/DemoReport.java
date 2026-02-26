package org.dynamisai.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates {@link TickRecord}s throughout the demo and writes a JSON summary to disk.
 */
public final class DemoReport {

    private final List<TickRecord> ticks = new ArrayList<>();
    private String finalOutcome = "unknown";

    /**
     * Adds a tick record to the report.
     *
     * @param tick The record for a single simulation tick.
     */
    public void add(TickRecord tick) {
        ticks.add(tick);
    }

    /**
     * Sets the final outcome of the scenario.
     *
     * @param outcome A description of how the scenario ended.
     */
    public void setOutcome(String outcome) {
        this.finalOutcome = outcome;
    }

    /**
     * Serializes the accumulated report data to a JSON file.
     *
     * @param path The file path where the JSON should be written.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void writeJson(Path path) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"scenario\": \"guard-patrol-encounter\",\n");
        sb.append("  \"totalTicks\": ").append(ticks.size()).append(",\n");
        sb.append("  \"outcome\": \"").append(escape(finalOutcome)).append("\",\n");
        sb.append("  \"ticks\": [\n");
        for (int i = 0; i < ticks.size(); i++) {
            TickRecord t = ticks.get(i);
            sb.append("    {\n");
            sb.append("      \"tick\": ").append(t.tick()).append(",\n");
            sb.append("      \"playerAction\": \"").append(escape(t.playerAction())).append("\",\n");
            sb.append("      \"playerSpeech\": \"").append(escape(t.playerSpeech())).append("\",\n");
            sb.append("      \"guard1Dialogue\": \"").append(escape(t.guard1Dialogue())).append("\",\n");
            sb.append("      \"guard2Dialogue\": \"").append(escape(t.guard2Dialogue())).append("\",\n");
            sb.append("      \"guard1Task\": \"").append(escape(t.guard1Task())).append("\",\n");
            sb.append("      \"guard2Task\": \"").append(escape(t.guard2Task())).append("\",\n");
            sb.append("      \"crowdLod\": \"").append(escape(t.crowdLod())).append("\",\n");
            sb.append("      \"social\": \"").append(escape(t.socialState())).append("\",\n");
            sb.append("      \"memory\": \"").append(escape(t.memoryEvent())).append("\",\n");
            sb.append("      \"perception\": \"").append(escape(t.perceptionSummary())).append("\"\n");
            sb.append("    }").append(i < ticks.size() - 1 ? "," : "").append("\n");
        }
        sb.append("  ]\n}\n");
        Files.writeString(path, sb.toString());
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
