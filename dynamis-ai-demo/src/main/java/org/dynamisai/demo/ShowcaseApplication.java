package org.dynamisai.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * JavaFX Application bootstrap.
 */
public final class ShowcaseApplication extends Application {

    private DemoWorld world;
    private ShowcaseGui gui;

    @Override
    public void start(Stage primaryStage) {
        world = new DemoWorld();
        gui = new ShowcaseGui(primaryStage, world);
        gui.show();
    }

    @Override
    public void stop() {
        if (gui != null) {
            gui.shutdown();
        }
        if (world != null) {
            world.shutdown();
        }
        Platform.exit();
    }
}
