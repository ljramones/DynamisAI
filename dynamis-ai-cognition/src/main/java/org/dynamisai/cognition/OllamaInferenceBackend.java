package org.dynamisai.cognition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Development backend using a locally-running Ollama server.
 * Not for shipped product — for faster dev iteration without loading Jlama weights.
 *
 * Requires: Ollama running at http://localhost:11434
 */
public final class OllamaInferenceBackend implements InferenceBackend {

    private static final Logger log = LoggerFactory.getLogger(OllamaInferenceBackend.class);

    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;
    private final AtomicInteger callCount = new AtomicInteger(0);
    private final AtomicInteger failCount = new AtomicInteger(0);
    private volatile long lastLatencyMs = 0;

    public OllamaInferenceBackend(String baseUrl, String model) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    public OllamaInferenceBackend() {
        this("http://localhost:11434", "llama3.2");
    }

    @Override
    public String generate(String prompt, GenerationConfig config) throws InferenceException {
        callCount.incrementAndGet();
        long start = System.currentTimeMillis();

        String body = buildRequestBody(prompt, config);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/generate"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(30))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
            lastLatencyMs = System.currentTimeMillis() - start;

            if (response.statusCode() != 200) {
                failCount.incrementAndGet();
                throw new InferenceException(
                    "Ollama returned HTTP " + response.statusCode());
            }
            return extractResponse(response.body());
        } catch (IOException | InterruptedException e) {
            failCount.incrementAndGet();
            lastLatencyMs = System.currentTimeMillis() - start;
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new InferenceException("Ollama request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpRequest ping = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tags"))
                .GET().timeout(Duration.ofSeconds(2)).build();
            HttpResponse<String> r = httpClient.send(ping, HttpResponse.BodyHandlers.ofString());
            return r.statusCode() == 200;
        } catch (Exception e) {
            log.debug("Ollama availability check failed", e);
            return false;
        }
    }

    @Override public boolean supportsStreaming() { return true; }
    @Override public String backendName() { return "OllamaBackend[" + model + "]"; }

    @Override
    public InferenceBackendMetrics getMetrics() {
        return new InferenceBackendMetrics(
            lastLatencyMs, 0f,
            callCount.get(), failCount.get(), true
        );
    }

    private String buildRequestBody(String prompt, GenerationConfig config) {
        return String.format(
            "{\"model\":\"%s\",\"prompt\":%s,\"stream\":false,\"options\":" +
            "{\"temperature\":%.2f,\"num_predict\":%d,\"seed\":%d}}",
            model,
            jsonString(prompt),
            config.temperature(),
            config.maxTokens(),
            config.seed()
        );
    }

    private String extractResponse(String body) {
        int idx = body.indexOf("\"response\":");
        if (idx < 0) return body;
        int start = body.indexOf('"', idx + 11) + 1;
        int end = body.indexOf('"', start);
        while (end > 0 && body.charAt(end - 1) == '\\') {
            end = body.indexOf('"', end + 1);
        }
        return end > start ? body.substring(start, end) : body;
    }

    private String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r") + "\"";
    }
}
