package agh.ics.oop.presenter;



import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.WorldDirections;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Presenter implements Observer {


    @FXML
    private Label infoLabel;
    @FXML
    private TextField movesTextField;
    @FXML
    private Label moveInfoLabel;
    @FXML
    private Canvas canvas;

    private final static int CELL_SIZE = 40;
    WorldMap worldMap;
    public void setWorldMap(WorldMap worldMap) {
        this.worldMap = worldMap;
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
        gc.setStroke(Color.DARKGREEN);
        int cols = upper.getY()-lower.getY()+2;
        int rows = upper.getX()-lower.getX()+2;

        for(int row=0;row<rows+1;row++){
            gc.strokeLine(row*CELL_SIZE,0,row*CELL_SIZE,cols*CELL_SIZE);
        }

        for(int col=0;col<cols+1;col++){
            gc.strokeLine(0,col*CELL_SIZE,rows*CELL_SIZE,col*CELL_SIZE);
        }
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
    @Override
    public void mapChanged(WorldMap worldmap, String Message) {
        Platform.runLater(()->{
            drawMap(worldMap);
//            funkcja zliczająca statystyki
//                    tutaj;
            moveInfoLabel.setText(moveInfoLabel.getText()+"\n"+ Message);
        });
    }

    public void onSimulationStartClicked() {
        WorldMap map = new WorldMap(5, 5);
        List<Animal> animals = new ArrayList<>(
                List.of(
                        new Animal(new Vector2d(2, 1)),
                        new Animal(new Vector2d(3, 4))
                )
        );

        Simulation simulation = new Simulation(conf);
        PresenterApp simulationApp = new PresenterApp();
        Platform.runLater(() -> {
            try {
                simulationApp.createStage(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread simulationThread = new Thread(() -> {
            try {
                simulation.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        simulationThread.setDaemon(true);
        simulationThread.start();
    }
}
