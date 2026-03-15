package org.dynamisengine.ai.demo;

import com.github.tjake.jlama.model.ModelSupport;
import com.github.tjake.jlama.safetensors.DType;
import com.github.tjake.jlama.util.Downloader;

import java.nio.file.Path;

/**
 * Pre-caches a HuggingFace model for Jlama.
 */
public final class ModelSetup {

    private static final Path MODEL_CACHE =
        Path.of(System.getProperty("user.home"), ".jlama", "models");

    private ModelSetup() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: ModelSetup <owner/model-name>");
            System.exit(1);
        }

        String modelName = args[0];
        String token = System.getProperty("JLAMA_HF_TOKEN", System.getenv("HF_TOKEN"));

        if (token == null || token.isBlank()) {
            System.err.println("ERROR: HF token not found.");
            System.err.println("  Set JLAMA_HF_TOKEN system property or HF_TOKEN env var.");
            System.exit(1);
        }

        System.out.printf("==> Downloading %s%n", modelName);
        System.out.printf("    Cache: %s%n", MODEL_CACHE);
        System.out.println("    This may take several minutes on first run...");

        var modelDir = new Downloader(MODEL_CACHE.toString(), modelName)
            .withAuthToken(token)
            .huggingFaceModel();
        var model = ModelSupport.loadModel(modelDir, DType.F32, DType.I8);

        System.out.printf("%n==> Cached: %s%n", model.getClass().getSimpleName());
        System.out.println("    Ready for inference.");

        try {
            model.close();
        } catch (Exception ignored) {
            // best effort
        }
    }
}
