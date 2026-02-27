package org.dynamisai.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.dynamisai.core.Location;
import org.dynamisai.crowd.CrowdLod;
import org.dynamisai.crowd.CrowdSnapshot;
import org.dynamisai.social.Relationship;
import org.dynamisai.social.RelationshipTag;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JavaFX showcase window with map, action log, and dialogue feed.
 */
public final class ShowcaseGui {

    private static final double WORLD_SIZE = 32.0;
    private static final double CANVAS_SIZE = 600.0;
    private static final double SCALE = CANVAS_SIZE / WORLD_SIZE;
    private static final int TICK_INTERVAL_MS = 500;

    private final Stage stage;
    private final DemoWorld world;

    private final Canvas mapCanvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);

    private final ObservableList<String> actionItems = FXCollections.observableArrayList();
    private final ObservableList<String> dialogueItems = FXCollections.observableArrayList();
    private final ListView<String> actionList = new ListView<>(actionItems);
    private final ListView<String> dialogueList = new ListView<>(dialogueItems);

    private final TextField speechField = new TextField();
    private final Label statusLabel = new Label("Ready");

    private final AtomicLong currentTick = new AtomicLong(0);
    private volatile PlayerAction pendingAction = PlayerAction.WAIT;
    private volatile String pendingSpeech = "";
    private volatile boolean awaitingInput = true;

    private final ScheduledExecutorService simExecutor =
        Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("sim-").factory());

    private AnimationTimer renderTimer;

    public ShowcaseGui(Stage stage, DemoWorld world) {
        this.stage = stage;
        this.world = world;
    }

    public void show() {
        stage.setTitle("DynamisAI Live Showcase - Guard Patrol Encounter");
        stage.setScene(buildScene());
        stage.setResizable(false);
        stage.show();

        startRenderLoop();
        startSimLoop();
    }

    public void shutdown() {
        if (renderTimer != null) {
            renderTimer.stop();
        }
        simExecutor.shutdownNow();
    }

    private Scene buildScene() {
        VBox mapPanel = new VBox(4);
        mapPanel.setPadding(new Insets(8));
        Label mapLabel = new Label("WORLD MAP  (32x32m)");
        mapLabel.setFont(Font.font("Monospaced", 11));
        mapPanel.getChildren().addAll(mapLabel, mapCanvas);

        actionList.setPrefHeight(260);
        actionList.setStyle("-fx-font-family: Monospaced; -fx-font-size: 11;");
        Label actionLabel = new Label("ACTIONS");
        actionLabel.setFont(Font.font("Monospaced", 11));
        VBox actionPanel = new VBox(4, actionLabel, actionList);
        actionPanel.setPadding(new Insets(8, 8, 0, 4));
        VBox.setVgrow(actionList, Priority.ALWAYS);

        dialogueList.setPrefHeight(260);
        dialogueList.setStyle("-fx-font-family: Monospaced; -fx-font-size: 11;");
        Label dialogueLabel = new Label("DIALOGUE");
        dialogueLabel.setFont(Font.font("Monospaced", 11));
        VBox dialoguePanel = new VBox(4, dialogueLabel, dialogueList);
        dialoguePanel.setPadding(new Insets(0, 8, 8, 4));
        VBox.setVgrow(dialogueList, Priority.ALWAYS);

        VBox rightColumn = new VBox(0, actionPanel, dialoguePanel);
        rightColumn.setPrefWidth(300);
        rightColumn.setStyle("-fx-border-color: #444; -fx-border-width: 0 0 0 1;");

        HBox topRow = new HBox(0, mapPanel, rightColumn);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        HBox inputBar = buildInputBar();
        inputBar.setPadding(new Insets(6, 8, 6, 8));
        inputBar.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #444; -fx-border-width: 1 0 0 0;");

        VBox root = new VBox(0, topRow, inputBar);
        root.setStyle("-fx-background-color: #0d0d1a;");

        Scene scene = new Scene(root, CANVAS_SIZE + 300, CANVAS_SIZE + 56);
        scene.setFill(Color.web("#0d0d1a"));

        scene.setOnKeyPressed(e -> {
            if (!awaitingInput) {
                return;
            }
            PlayerAction action = switch (e.getCode()) {
                case A -> PlayerAction.APPROACH;
                case H -> PlayerAction.HOSTILE;
                case F -> PlayerAction.FLEE;
                case W -> PlayerAction.WAIT;
                case S -> PlayerAction.SPEAK;
                default -> null;
            };
            if (action != null) {
                submitAction(action);
            }
        });

        return scene;
    }

    private HBox buildInputBar() {
        Label hint = new Label("[A]pproach  [H]ostile  [F]lee  [W]ait  [S]peak:");
        hint.setFont(Font.font("Monospaced", 11));
        hint.setTextFill(Color.LIGHTGRAY);

        speechField.setPromptText("Speech text (Enter to submit with [S])...");
        speechField.setFont(Font.font("Monospaced", 11));
        speechField.setPrefWidth(280);
        speechField.setStyle("-fx-background-color: #1a1a3e; -fx-text-fill: white;");
        speechField.setOnAction(e -> submitAction(PlayerAction.SPEAK));

        statusLabel.setFont(Font.font("Monospaced", 11));
        statusLabel.setTextFill(Color.YELLOW);
        statusLabel.setPrefWidth(180);

        HBox bar = new HBox(12, hint, speechField, statusLabel);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return bar;
    }

    private void submitAction(PlayerAction action) {
        if (!awaitingInput) {
            return;
        }
        pendingAction = action;
        pendingSpeech = action == PlayerAction.SPEAK ? speechField.getText() : "";
        awaitingInput = false;
        speechField.clear();
    }

    private void startRenderLoop() {
        renderTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderMap();
            }
        };
        renderTimer.start();
    }

    private void renderMap() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        CrowdSnapshot snap = world.crowd.latestSnapshot();

        gc.setFill(Color.web("#0d0d1a"));
        gc.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        gc.setStroke(Color.web("#1a1a2e"));
        gc.setLineWidth(0.5);
        for (int i = 0; i <= 32; i += 2) {
            double px = i * SCALE;
            gc.strokeLine(px, 0, px, CANVAS_SIZE);
            gc.strokeLine(0, px, CANVAS_SIZE, px);
        }

        gc.setStroke(Color.web("#2244aa", 0.5));
        gc.setLineWidth(1);
        Location[] wps = DemoWorld.WAYPOINTS;
        for (int i = 0; i < wps.length; i++) {
            Location a = wps[i];
            Location b = wps[(i + 1) % wps.length];
            gc.strokeLine(wx(a.x()), wy(a.z()), wx(b.x()), wy(b.z()));
        }

        Relationship rel = world.social.graph().get(world.guard1.id, world.player.id);
        Color relColor = relColor(rel);
        gc.setStroke(relColor.deriveColor(0, 1, 1, 0.4));
        gc.setLineWidth(2);
        gc.strokeLine(
            wx(world.guard1.position.x()), wy(world.guard1.position.z()),
            wx(world.player.position.x()), wy(world.player.position.z())
        );

        if (snap.groups().containsKey(world.patrolGroup)) {
            var groupSnap = snap.groups().get(world.patrolGroup);
            if (groupSnap.lod() == CrowdLod.FULL || groupSnap.lod() == CrowdLod.REDUCED) {
                gc.setFill(Color.web("#ffffff", 0.12));
                for (var agent : groupSnap.agents()) {
                    double x = wx(agent.position().x());
                    double y = wy(agent.position().z());
                    gc.fillOval(x - 6, y - 6, 12, 12);
                }
            }
        }

        drawNpc(gc, world.guard1.position, lodColor(snap, world.guard1.id), "G1",
            world.guard1.currentTask, world.guard1.isAlert);
        drawNpc(gc, world.guard2.position, lodColor(snap, world.guard2.id), "G2",
            world.guard2.currentTask, world.guard2.isAlert);
        drawPlayer(gc, world.player.position);
        drawLodLegend(gc, snap);

        gc.setFill(Color.web("#888888"));
        gc.setFont(Font.font("Monospaced", 10));
        gc.fillText("TICK " + currentTick.get() + " / 20", 8, CANVAS_SIZE - 8);
    }

    private void drawNpc(GraphicsContext gc, Location pos,
                         Color color, String label,
                         String task, boolean alert) {
        double x = wx(pos.x());
        double y = wy(pos.z());
        double r = 10;

        if (alert) {
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(1.5);
            gc.strokeOval(x - r - 4, y - r - 4, (r + 4) * 2, (r + 4) * 2);
        }

        gc.setFill(color);
        gc.fillOval(x - r, y - r, r * 2, r * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeOval(x - r, y - r, r * 2, r * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", 10));
        gc.fillText(label, x - 6, y + 4);

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Monospaced", 9));
        gc.fillText(task, x - r, y + r + 12);
    }

    private void drawPlayer(GraphicsContext gc, Location pos) {
        double x = wx(pos.x());
        double y = wy(pos.z());
        double r = 10;

        gc.setFill(Color.YELLOW);
        gc.fillPolygon(
            new double[]{x, x + r * 0.4, x + r, x + r * 0.4, x, x - r * 0.4, x - r, x - r * 0.4},
            new double[]{y - r, y - r * 0.4, y, y + r * 0.4, y + r, y + r * 0.4, y, y - r * 0.4},
            8);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", 9));
        gc.fillText("YOU", x - 8, y + r + 12);
    }

    private void drawLodLegend(GraphicsContext gc, CrowdSnapshot snap) {
        String lodStr = snap.groups().containsKey(world.patrolGroup)
            ? snap.groups().get(world.patrolGroup).lod().name()
            : "UNKNOWN";

        gc.setFill(Color.web("#333355"));
        gc.fillRoundRect(8, 8, 140, 56, 6, 6);

        gc.setFont(Font.font("Monospaced", 9));
        gc.setFill(Color.GREEN);
        gc.fillOval(14, 18, 8, 8);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("FULL  (< 20m)", 28, 26);

        gc.setFill(Color.YELLOW);
        gc.fillOval(14, 32, 8, 8);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("REDUCED (60m)", 28, 40);

        gc.setFill(Color.GRAY);
        gc.fillOval(14, 46, 8, 8);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("SKEL / CULLED", 28, 54);

        gc.setFill(Color.CYAN);
        gc.setFont(Font.font("Monospaced", 10));
        gc.fillText("LOD: " + lodStr, 8, 78);
    }

    private void startSimLoop() {
        updateStatus("Press A/H/F/W/S to act");

        simExecutor.scheduleAtFixedRate(() -> {
            try {
                simTick();
            } catch (Exception e) {
                Platform.runLater(() -> updateStatus("ERROR: " + e.getMessage()));
            }
        }, 1000, TICK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void simTick() throws Exception {
        long tick = currentTick.incrementAndGet();
        if (tick > 20 || world.isResolved()) {
            simExecutor.shutdown();
            String outcome = world.isResolved() ? world.outcome() : "20 ticks elapsed - patrol continues";
            world.report().setOutcome(outcome);
            Platform.runLater(() -> {
                addDialogue("=== OUTCOME: " + outcome + " ===");
                updateStatus("Demo complete");
                writeReport();
            });
            return;
        }

        Platform.runLater(() -> updateStatus("Tick " + tick + "/20 - your action?"));

        awaitingInput = true;
        long waited = 0;
        while (awaitingInput && waited < 30_000) {
            Thread.sleep(50);
            waited += 50;
        }
        if (awaitingInput) {
            pendingAction = PlayerAction.WAIT;
            pendingSpeech = "";
        }

        PlayerAction action = pendingAction;
        String speech = pendingSpeech;
        awaitingInput = false;

        TickRecord record = world.tick(tick, action, speech);

        Platform.runLater(() -> {
            addAction(String.format("[%2d] %-8s %s",
                tick, action.name(), speech.isEmpty() ? "" : "\"" + speech + "\""));

            addAction(String.format("     G1:%-18s G2:%s", record.guard1Task(), record.guard2Task()));
            addAction("     Social: " + record.socialState());

            if (!record.guard1Dialogue().isEmpty()) {
                addDialogue("[" + tick + "] Guard1: " + record.guard1Dialogue());
            }
            if (!speech.isEmpty()) {
                addDialogue("[" + tick + "] You:    " + speech);
            }

            updateStatus("Tick " + tick + " done - " + record.crowdLod());
        });
    }

    private void addAction(String s) {
        actionItems.add(s);
        if (actionItems.size() > 200) {
            actionItems.remove(0);
        }
        actionList.scrollTo(actionItems.size() - 1);
    }

    private void addDialogue(String s) {
        dialogueItems.add(s);
        if (dialogueItems.size() > 100) {
            dialogueItems.remove(0);
        }
        dialogueList.scrollTo(dialogueItems.size() - 1);
    }

    private void updateStatus(String s) {
        statusLabel.setText(s);
    }

    private void writeReport() {
        try {
            var path = java.nio.file.Path.of("demo-report.json");
            world.report().writeJson(path);
            addAction("Report written: " + path.toAbsolutePath());
        } catch (Exception e) {
            addAction("Report write failed: " + e.getMessage());
        }
    }

    private double wx(double worldX) {
        return worldX * SCALE;
    }

    private double wy(double worldZ) {
        return worldZ * SCALE;
    }

    private Color lodColor(CrowdSnapshot snap, org.dynamisai.core.EntityId id) {
        return snap.groupOf(id)
            .map(g -> switch (g.lod()) {
                case FULL -> Color.web("#22cc44");
                case REDUCED -> Color.web("#cccc22");
                case SKELETON -> Color.web("#888888");
                case CULLED -> Color.web("#444444");
            })
            .orElse(Color.GRAY);
    }

    private Color relColor(Relationship rel) {
        if (rel.hasTag(RelationshipTag.ENEMY) || rel.hasTag(RelationshipTag.BETRAYED)) {
            return Color.RED;
        }
        if (rel.hasTag(RelationshipTag.ALLY) || rel.hasTag(RelationshipTag.TRUSTED)) {
            return Color.GREEN;
        }
        return Color.web("#446688");
    }
}
