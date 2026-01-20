package agh.ics.oop.presenter;



import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.WorldDirections;
import agh.ics.oop.model.world.element.WorldElement;
import agh.ics.oop.model.world.map.WorldMap;
import agh.ics.oop.simulations.Simulation;
import agh.ics.oop.util.AnimalConfig;
import agh.ics.oop.util.SimulationState;
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

import java.lang.reflect.Array;
import java.util.*;

public class Presenter implements Observer {
    private double offsetX;
    private double offsetY;
    private double cellSize;
    private Simulation simulation;
    private List<SimulationState> historyBuffer;
    private int currentDisplayIndex = -1;
    private boolean browsingHistory = false;

    @FXML private VBox statsPanel;
    @FXML private Label moveInfoLabel;
    @FXML private Canvas canvas;
    @FXML private Button pauseButton;
    @FXML private Button previousButton;
    @FXML private Button nextButton;

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
        if (browsingHistory) return;

        Platform.runLater(()->{
            drawMap(newWorldMap);
            moveInfoLabel.setText(message);
        });
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public void onPauseButtonClicked() {
        if (simulation == null) return;

        simulation.togglePause();
        boolean isPaused = simulation.isPaused();

        Platform.runLater(() -> {
            pauseButton.setText(isPaused ? "Resume" : "Pause");

            if (isPaused) {
                synchronized (this) {
                    browsingHistory = true;

                    historyBuffer = simulation.getHistory();
                    if (historyBuffer != null && historyBuffer.size() > 1) {
                        currentDisplayIndex = historyBuffer.size() - 1;
                        previousButton.setDisable(false);
                        nextButton.setDisable(true);
                        renderFrame(currentDisplayIndex);
                    }
                }
            } else {
                synchronized (this) {
                    browsingHistory = false;

                    previousButton.setDisable(true);
                    nextButton.setDisable(true);
                }
            }
        });
    }


    public void onPreviousButtonClicked() {
        if (!browsingHistory || currentDisplayIndex <= 0) return;

        currentDisplayIndex--;
        renderFrame(currentDisplayIndex);

        previousButton.setDisable(currentDisplayIndex == 0);
        nextButton.setDisable(false);
    }

    public void onNextButtonClicked() {
        if (!browsingHistory || currentDisplayIndex >= historyBuffer.size() - 1) return;

        currentDisplayIndex++;
        renderFrame(currentDisplayIndex);

        nextButton.setDisable(currentDisplayIndex == historyBuffer.size() - 1);
        previousButton.setDisable(false);
    }


    public void renderFrame(int index) {
        if (historyBuffer == null || index < 0 || index >= historyBuffer.size()) return;

        SimulationState state = historyBuffer.get(index);

        Platform.runLater(() -> {
            drawMapFromState(state);
            moveInfoLabel.setText(state.statistics());
        });
    }

    private double mapX(int x, Vector2d lower) {
        return offsetX + (x - lower.getX() + 1) * cellSize;
    }

    private double mapY(int y, Vector2d lower, Vector2d upper) {
        int mapHeight = upper.getY() - lower.getY() + 1;
        return offsetY + (mapHeight - (y - lower.getY()) ) * cellSize;
    }
    public Color getEnergyColor(double energy) {
        double n = (double) simulation.getWorldMap().getConfig().energy().minimumToReproduce()/5;
        if (energy <  n) return Color.rgb(173, 216, 230);
        if (energy < 2*n) return Color.rgb(100, 149, 237);
        if (energy < 3*n) return Color.rgb(123, 104, 238);
        if (energy < 4*n) return Color.rgb(72, 61, 139);
        if (energy < 5*n) return Color.rgb(75, 0, 130);
        else return Color.rgb(48, 0, 96);
    }
    public Color getAnimalColor(Animal animal) {
        double energy = animal.getLifeEnergy();
        return getEnergyColor(energy);
    }
    public void drawMapFromState(SimulationState state) {
        Vector2d lower = simulation.getWorldMap().getLowerLeft();
        Vector2d upper = simulation.getWorldMap().getUpperRight();

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
        drawPopularGrassPositions(gc,state.popularGrassPositions(),state.jungleSize(), lower, upper);
        drawGrid(gc, lower, upper);
        drawJungle(gc, lower, upper);
        drawAxis(gc, lower, upper);

        drawGrassFromState(gc, state, lower, upper);
        drawAnimalsFromState(gc, state, lower, upper);
    }

    private void drawGrassFromState(
            GraphicsContext gc,
            SimulationState state,
            Vector2d lower,
            Vector2d upper
    ) {

        for (Map.Entry<Vector2d, Boolean> entry : state.grassPositions().entrySet()) {
            Color color = Color.BLACK;
            if (entry.getValue()) color = Color.RED;
            configureFont(gc, (int)(cellSize * 0.8), color);

            double px = mapX(entry.getKey().getX(), lower);
            double py = mapY(entry.getKey().getY(), lower, upper);

            gc.fillText("*", px + cellSize / 2, py + cellSize / 2);
        }
    }

    private void drawAnimalsFromState(
            GraphicsContext gc,
            SimulationState state,
            Vector2d lower,
            Vector2d upper
    ) {
        for (Map.Entry<Vector2d, List<AnimalConfig>> entry
                : state.animalsPositions().entrySet()) {

            Vector2d position = entry.getKey();
            List<AnimalConfig> animals = entry.getValue();

            double px = mapX(position.getX(), lower);
            double py = mapY(position.getY(), lower, upper);

            for (int i = 0; i < animals.size(); i++) {
                AnimalConfig animal = animals.get(i);

                Color color = getEnergyColor(animal.lifeEnergy());
                if (animal.isMostPopularGene()) color = Color.GOLD;
                if (animal.isBurning()) color = Color.RED;

                configureFont(gc, (int)(cellSize * 0.8), color);

                String symbol = directionSymbol(animal.facingDirection());

                gc.fillText(
                        symbol,
                        px + cellSize / 2,
                        py + cellSize / 2
                );
            }
        }
    }

    private String directionSymbol(WorldDirections dir) {
        return switch (dir) {
            case NORTH,NORTH_EAST,NORTH_WEST -> "^";
            case SOUTH,SOUTH_EAST,SOUTH_WEST -> "v";
            case WEST -> "<";
            case EAST -> ">";
        };
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
        drawPopularGrassPositions(gc,worldMap.getPopularGrassPositions(),worldMap.getJungleSize(),lower, upper);
        drawGrid(gc, lower, upper);
        drawJungle(gc, lower, upper);
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
        gc.setLineWidth(2);
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
        int mapHeight = upper.getY() - lower.getY() + 1;

        int jungleHeight = Math.max(1, (int)(mapHeight * 0.2));
        int minY = (mapHeight - jungleHeight) / 2;

        int jungleMinX = lower.getX();
        int jungleMinY = lower.getY()+minY;
        int jungleMaxX = upper.getX();
        int jungleMaxY = jungleMinY+jungleHeight-1;

        gc.setLineWidth(1);

        for (int i = jungleMinX; i <= jungleMaxX; i++) {
            for (int j = jungleMinY; j <= jungleMaxY; j++) {
                double px = mapX(i,lower);
                double py = mapY(j,lower,upper);

                gc.setStroke(Color.WHITE);
                gc.strokeRect(px,py,cellSize,cellSize);
            }
        }
    }

    private void drawWorldElements(GraphicsContext gc, WorldMap worldMap, Vector2d lower, Vector2d upper){
        for (WorldElement element : worldMap.getAllElements()) {
            Vector2d pos = element.getPosition();
            double px = mapX(pos.getX(), lower);
            double py = mapY(pos.getY(), lower, upper);

            Color color = Color.BLACK;
            if (element instanceof Animal && ((Animal) element).isMostPopularGene()) color = Color.GOLD;
            else if (element instanceof Animal) color = getAnimalColor((Animal) element);
            if (element.getIsBurning()) color = Color.RED;
            configureFont(gc, (int)(cellSize*0.8), color);
            gc.fillText(element.toString(), px + cellSize/2, py + cellSize/2);
        }
    }
    private void drawPopularGrassPositions(GraphicsContext gc,Map<Vector2d,Integer> popularGrassPositions,int jungleSize, Vector2d lower, Vector2d upper) {
        List<Map.Entry<Vector2d,Integer>>sortedPopularPositions = sortPopularGrassPositions(popularGrassPositions);
        for (int i = 0; i < Math.min(jungleSize,sortedPopularPositions.size()); i++) {
            paintCell(gc,sortedPopularPositions.get(i).getKey(),lower,upper,Color.rgb(34, 139, 34));
        }
    }
    private List<Map.Entry<Vector2d,Integer>> sortPopularGrassPositions(Map<Vector2d,Integer> popularGrassPositions){
        List<Map.Entry<Vector2d, Integer>> sortedPopularPositions =
                new ArrayList<>(popularGrassPositions.entrySet());
        sortedPopularPositions.sort(Map.Entry.<Vector2d,Integer>comparingByValue().reversed());
        return sortedPopularPositions;
    }
    private void paintCell(
            GraphicsContext gc,
            Vector2d position,
            Vector2d lower,
            Vector2d upper,
            Color color
    ) {
        double x = mapX(position.getX(), lower);
        double y = mapY(position.getY(), lower, upper);

        gc.setFill(color);
        gc.fillRect(x, y, cellSize, cellSize);
    }

    private void configureFont(GraphicsContext graphics, int size, Color color) {
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        graphics.setFont(new Font("Arial", size));
        graphics.setFill(color);
    }


}