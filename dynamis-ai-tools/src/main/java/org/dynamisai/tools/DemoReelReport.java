package org.dynamisai.tools;

import java.util.List;

/**
 * Full report from a demo reel run — serialised to JSON for inspection.
 */
public record DemoReelReport(
    String npcId,
    int totalTicks,
    int successfulDialogueTicks,
    int fallbackDialogueTicks,
    int totalPercepts,
    int totalMemoryEvents,
    List<DemoTickResult> tickResults
) {
    public boolean allTicksSucceeded() {
        return tickResults.size() == totalTicks;
    }

    /** Simple JSON serialisation — no library dependency. */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"npcId\": \"").append(npcId).append("\",\n");
        sb.append("  \"totalTicks\": ").append(totalTicks).append(",\n");
        sb.append("  \"successfulDialogueTicks\": ").append(successfulDialogueTicks).append(",\n");
        sb.append("  \"fallbackDialogueTicks\": ").append(fallbackDialogueTicks).append(",\n");
        sb.append("  \"totalPercepts\": ").append(totalPercepts).append(",\n");
        sb.append("  \"totalMemoryEvents\": ").append(totalMemoryEvents).append(",\n");
        sb.append("  \"tickResults\": [\n");
        for (int i = 0; i < tickResults.size(); i++) {
            DemoTickResult r = tickResults.get(i);
            sb.append("    {");
            sb.append("\"tick\":").append(r.tick()).append(",");
            sb.append("\"perceptCount\":").append(r.perceptCount()).append(",");
            sb.append("\"threat\":\"").append(r.aggregateThreat()).append("\",");
            sb.append("\"dialogue\":\"")
                .append(r.dialogueText().replace("\\", "\\\\").replace("\"", "\\\"")).append("\",");
            sb.append("\"fromCache\":").append(r.dialogueFromCache()).append(",");
            sb.append("\"frameBudgetMs\":").append(r.frameBudgetMs());
            sb.append("}");
            if (i < tickResults.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }
}
