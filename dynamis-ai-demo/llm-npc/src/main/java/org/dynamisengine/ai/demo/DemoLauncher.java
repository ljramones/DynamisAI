package org.dynamisengine.ai.demo;

import javafx.application.Application;

import java.util.Arrays;

/**
 * Entry point: CLI by default, JavaFX showcase with --gui.
 */
public final class DemoLauncher {

    private DemoLauncher() {
    }

    public static void main(String[] args) throws Exception {
        if (Arrays.asList(args).contains("--gui")) {
            Application.launch(ShowcaseApplication.class, args);
        } else {
            DynamisAiDemo.main(args);
        }
    }
}
