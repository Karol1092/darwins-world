package agh.ics.oop.presenter;



import agh.ics.oop.model.world.element.WorldElement;
import agh.ics.oop.model.world.map.WorldMap;
import agh.ics.oop.simulations.Simulation;
import agh.ics.oop.util.Vector2d;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class Presenter implements Observer {
    private double offsetX;
    private double offsetY;
    private double cellSize;
    private Simulation simulation;

    @FXML private VBox statsPanel;
    @FXML private Label moveInfoLabel;
    @FXML private Canvas canvas;
    @FXML private Button pauseButton;
    @FXML
    public void initialize() {
        canvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                canvas.widthProperty().bind(newScene.widthProperty().multiply(0.8));
                canvas.heightProperty().bind(newScene.heightProperty().subtract(50));
                statsPanel.prefWidthProperty().bind(newScene.widthProperty().multiply(0.2));
            }
        });}

    @Override
    public void mapChanged(WorldMap newWorldMap, String message) {
        Platform.runLater(()->{
            drawMap(newWorldMap);
            moveInfoLabel.setText(message);
        });
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public void onPauseButtonClicked() {
        if (simulation != null) {
            simulation.togglePause();
            Platform.runLater(() -> {
                pauseButton.setText(simulation.isPaused() ? "Resume" : "Pause");
            });
        }
    }

    private double mapX(int x, Vector2d lower) {
        return offsetX + (x - lower.getX() + 1) * cellSize;
    }

    private double mapY(int y, Vector2d lower, Vector2d upper) {
        int mapHeight = upper.getY() - lower.getY() + 1;
        return offsetY + (mapHeight - (y - lower.getY()) ) * cellSize;
    }

    public void drawMap(WorldMap worldMap) {
        Vector2d lower = worldMap.getLowerLeft();
        Vector2d upper = worldMap.getUpperRight();

        int mapWidth = upper.getX() - lower.getX() + 1;
        int mapHeight = upper.getY() - lower.getY() + 1;

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        int totalCols = mapHeight + 1;
        int totalRows = mapWidth + 1;

        cellSize = Math.min(canvasWidth / totalRows, canvasHeight / totalCols);

        offsetX = (canvasWidth - cellSize * totalRows) / 2;
        offsetY = (canvasHeight - cellSize * totalCols) / 2;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        clearGrid(mapWidth, mapHeight);
        drawJungle(gc, lower, upper);
        drawGrid(gc, lower, upper);
        drawAxis(gc, lower, upper);
        drawWorldElements(gc, worldMap, lower, upper);
    }

    private void clearGrid(int mapWidth, int mapHeight){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.LIGHTGREEN);
        gc.fillRect(offsetX, offsetY, (mapWidth + 1) * cellSize, (mapHeight + 1) * cellSize);
    }

    private void drawGrid(GraphicsContext gc, Vector2d lower, Vector2d upper) {
        gc.setStroke(Color.BLACK);
        int rows = upper.getX() - lower.getX() + 1;
        int cols = upper.getY() - lower.getY() + 1;

        for (int r = -1; r <= rows; r++) {
            double x = mapX(lower.getX() + r, lower);
            gc.strokeLine(x, offsetY, x, offsetY + (cols + 1) * cellSize);
        }

        for (int c = -1; c <= cols; c++) {
            double y = mapY(lower.getY() + c, lower, upper);
            gc.strokeLine(offsetX, y, offsetX + (rows + 1) * cellSize, y);
        }
    }

    private void drawAxis(GraphicsContext gc, Vector2d lower, Vector2d upper) {
        int rows = upper.getX() - lower.getX() + 1;
        int cols = upper.getY() - lower.getY() + 1;

        configureFont(gc, (int)(cellSize / 2), Color.BLACK);

        for (int x = 0; x < rows; x++) {
            gc.fillText(String.valueOf(lower.getX() + x),
                    mapX(lower.getX() + x, lower) + cellSize/2,
                    offsetY + cellSize/2);
        }

        for (int y = 0; y < cols; y++) {
            gc.fillText(String.valueOf(lower.getY() + y),
                    offsetX + cellSize/2,
                    mapY(lower.getY() + y, lower, upper) + cellSize/2);
        }

        gc.fillText("y/x", offsetX + cellSize/2, offsetY + cellSize/2);
    }

    private void drawJungle(GraphicsContext gc, Vector2d lower, Vector2d upper) {
        int mapWidth = upper.getX() - lower.getX() + 1;
        int mapHeight = upper.getY() - lower.getY() + 1;
        int jungleHeight = Math.max(1, (int)(mapHeight * 0.2));
        int minY = (mapHeight - jungleHeight) / 2;

        gc.setFill(Color.DARKGREEN);
        gc.fillRect(
                offsetX,
                mapY(lower.getY() + minY + jungleHeight - 1, lower, upper),
                mapWidth * cellSize,
                jungleHeight * cellSize
        );
    }

    private void drawWorldElements(GraphicsContext gc, WorldMap worldMap, Vector2d lower, Vector2d upper){
        for (WorldElement element : worldMap.getAllElements()) {
            Vector2d pos = element.getPosition();
            double px = mapX(pos.getX(), lower);
            double py = mapY(pos.getY(), lower, upper);

            if (element.getIsBurning()) configureFont(gc, (int)(cellSize*0.8), Color.RED);
            else configureFont(gc, (int)(cellSize*0.8), Color.BLACK);

            gc.fillText(element.toString(), px + cellSize/2, py + cellSize/2);
        }
    }

    private void configureFont(GraphicsContext graphics, int size, Color color) {
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        graphics.setFont(new Font("Arial", size));
        graphics.setFill(color);
    }


}