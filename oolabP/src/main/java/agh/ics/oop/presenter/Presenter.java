package agh.ics.oop.presenter;



import agh.ics.oop.model.world.element.WorldElement;
import agh.ics.oop.model.world.map.WorldMap;
import agh.ics.oop.simulations.Simulation;
import agh.ics.oop.util.SimulationConfig;
import agh.ics.oop.util.Vector2d;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;

public class Presenter implements Observer {


    @FXML private Label moveInfoLabel;
    @FXML private Canvas canvas;
    @FXML private Button pauseButton;

    @FXML private TextField mapHeightTextField;
    @FXML private TextField mapWidthTextField;
    @FXML private TextField grassNumberTextField;
    @FXML private TextField grassSpawnNumberTextField;

    @FXML private TextField grassEnergyTextField;
    @FXML private TextField energyLossTextField;
    @FXML private TextField minimumEnergyTextField;
    @FXML private TextField reproduceEnergyLossTextField;

    @FXML private TextField animalNumberField;
    @FXML private TextField startEnergyTextField;

    @FXML private TextField minimumMutationsTextField;
    @FXML private TextField maximumMutationsTextField;
    @FXML private TextField geneLengthTextField;

    private final static int CELL_SIZE = 40;
    private Simulation simulation;

    @Override
    public void mapChanged(WorldMap newWorldMap, String Message) {
        Platform.runLater(()->{
            drawMap(newWorldMap);
            moveInfoLabel.setText(Message);
        });
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public void onSimulationStartClicked() {
        SimulationConfig config = getConfigFromUI();

        Simulation simulation = new Simulation(config);

        try {
            startSimulationWindow(simulation);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Thread simulationThread = new Thread(simulation);
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    private void startSimulationWindow(Simulation simulation) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("newSimulation.fxml"));
        BorderPane viewRoot = loader.load();

        Presenter presenter = loader.getController();

        presenter.setSimulation(simulation);

        WorldMap map = simulation.getWorldMap();
        map.addObserver(presenter);

        Stage stage = new Stage();
        stage.setTitle("Simulation Window");
        stage.setScene(new Scene(viewRoot));

        presenter.drawMap(map);
        stage.show();
    }

    public void onPauseButtonClicked() {
        if (simulation != null) {
            simulation.togglePause();
            Platform.runLater(() -> {
                pauseButton.setText(simulation.isPaused() ? "Resume" : "Pause");
            });
        }
    }

    private int getIntFromTextField(TextField textField) {
        String text = textField.getText();
        if (text == null || text.trim().isEmpty()) {
            text = textField.getPromptText();
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private SimulationConfig getConfigFromUI() {
        var mapConfig = new SimulationConfig.Map(
                getIntFromTextField(mapHeightTextField),
                getIntFromTextField(mapWidthTextField),
                getIntFromTextField(grassNumberTextField),
                getIntFromTextField(grassSpawnNumberTextField)
        );

        var energyConfig = new SimulationConfig.Energy(
                getIntFromTextField(grassEnergyTextField),
                getIntFromTextField(energyLossTextField),
                getIntFromTextField(minimumEnergyTextField),
                getIntFromTextField(reproduceEnergyLossTextField)
        );

        var animalConfig = new SimulationConfig.Animal(
                getIntFromTextField(animalNumberField),
                getIntFromTextField(startEnergyTextField)
        );

        var genotypeConfig = new SimulationConfig.Genotype(
                getIntFromTextField(minimumMutationsTextField),
                getIntFromTextField(maximumMutationsTextField),
                getIntFromTextField(geneLengthTextField)
        );

        return new SimulationConfig(
                mapConfig,
                energyConfig,
                animalConfig,
                genotypeConfig
        );
    }

    public void drawMap(WorldMap worldMap){

        Vector2d lower = worldMap.getLowerLeft();
        Vector2d upper = worldMap.getUpperRight();
        int width = worldMap.getUpperRight().getX()-worldMap.getLowerLeft().getX()+1;
        int height = worldMap.getUpperRight().getY()-worldMap.getLowerLeft().getY()+1;

        canvas.setWidth((width+1)* CELL_SIZE);
        canvas.setHeight((height+1)* CELL_SIZE);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        clearGrid();
        drawJungle(gc,width,height);
        drawGrid(gc,lower,upper);
        drawAxis(gc,lower,upper);
        drawWorldElements(gc,worldMap,lower,upper);
    }
    private void clearGrid(){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.LIGHTGREEN);
        gc.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
    }
    private void drawGrid(GraphicsContext gc,Vector2d lower,Vector2d upper){
        gc.setStroke(Color.BLACK);
        int cols = upper.getY()-lower.getY()+2;
        int rows = upper.getX()-lower.getX()+2;

        for(int row=0;row<rows+1;row++){
            gc.strokeLine(row*CELL_SIZE,0,row*CELL_SIZE,cols*CELL_SIZE);
        }

        for(int col=0;col<cols+1;col++){
            gc.strokeLine(0,col*CELL_SIZE,rows*CELL_SIZE,col*CELL_SIZE);
        }
    }
    private void drawJungle(GraphicsContext gc, int width, int height) {
        int jungleHeight = Math.max(1, (int)Math.round(height * 0.2));

        int minY = (height - jungleHeight) / 2;
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(CELL_SIZE, (minY+1) * CELL_SIZE, (width + 1) * CELL_SIZE, jungleHeight* CELL_SIZE);
    }

    private void drawAxis(GraphicsContext gc,Vector2d lower,Vector2d upper){
        int cols = upper.getY()-lower.getY()+1;
        int rows = upper.getX()-lower.getX()+1;
        configureFont(gc,(int)(CELL_SIZE/2),Color.BLACK);
        gc.fillText("y/x",CELL_SIZE/2,CELL_SIZE/2);

        for (int x=0;x<rows+1;x++){
            gc.fillText(String.valueOf(lower.getX()+x),(x+1)*CELL_SIZE+CELL_SIZE/2,CELL_SIZE/2);
        }
        for (int y=0;y<cols+1;y++){
            gc.fillText(String.valueOf(upper.getY()-y),CELL_SIZE/2,(y+1)*CELL_SIZE+CELL_SIZE/2);
        }
    }

    private void drawWorldElements(GraphicsContext gc,WorldMap worldMap,Vector2d lower,Vector2d upper){
        configureFont(gc,50,Color.BLACK);
        for (WorldElement element: worldMap.getAllElements()){
            Vector2d position = element.getPosition();
            int x = position.getX()-lower.getX()+1;
            int y = upper.getY() - position.getY()+1;
            gc.fillText(element.toString(), x*CELL_SIZE+CELL_SIZE/2, y*CELL_SIZE+CELL_SIZE/2);
        }
    }

    private void configureFont(GraphicsContext graphics, int size, Color black) {
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        graphics.setFont(new Font("Arial", size));
        graphics.setFill(black);
    }
}