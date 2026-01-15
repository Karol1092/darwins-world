package agh.ics.oop.presenter;


import agh.ics.oop.model.world.map.WorldMap;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
public class PresenterApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(); // zainicjowanie wczytywania FXML

        // wczytanie zasobu z katalogu resources (uniwersalny sposób)
        loader.setLocation(getClass().getClassLoader().getResource("simulation.fxml"));

        // Wczytanie FXML, konwersja FXML -> obiekty w Javie
        BorderPane viewRoot = loader.load();
//        SimulationPresenter presenter = loader.getController();

        configureStage(primaryStage,viewRoot);
        primaryStage.show();
    }
    public void createStage(WorldMap map) throws Exception{
        FXMLLoader loader = new FXMLLoader(); // zainicjowanie wczytywania FXML
        // wczytanie zasobu z katalogu resources (uniwersalny sposób)
        loader.setLocation(getClass().getClassLoader().getResource("new_simulation.fxml"));

        // Wczytanie FXML, konwersja FXML -> obiekty w Javie
        BorderPane viewRoot = loader.load();
        Presenter presenter = loader.getController();

        map.addObserver(presenter);
        Stage stage = new Stage();
        configureStage(stage,viewRoot);
        stage.show();
    }
    private void configureStage(Stage primaryStage, BorderPane viewRoot) {
        // stworzenie sceny (panelu do wyświetlania wraz zawartością z FXML)
        var scene = new Scene(viewRoot);

        // ustawienie sceny w oknie
        primaryStage.setScene(scene);

        // konfiguracja okna
        primaryStage.setTitle("Simulation app");
        primaryStage.minWidthProperty().bind(viewRoot.minWidthProperty());
        primaryStage.minHeightProperty().bind(viewRoot.minHeightProperty());

    }

}
