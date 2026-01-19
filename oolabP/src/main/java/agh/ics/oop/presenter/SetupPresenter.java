package agh.ics.oop.presenter;



import agh.ics.oop.model.world.map.WorldMap;
import agh.ics.oop.simulations.Simulation;
import agh.ics.oop.util.SimulationConfig;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

public class SetupPresenter {

    @FXML
    private TextField mapHeightTextField;
    @FXML
    private TextField mapWidthTextField;
    @FXML
    private TextField grassNumberTextField;
    @FXML
    private TextField grassSpawnNumberTextField;

    @FXML
    private TextField grassEnergyTextField;
    @FXML
    private TextField energyLossTextField;
    @FXML
    private TextField minimumEnergyTextField;
    @FXML
    private TextField reproduceEnergyLossTextField;

    @FXML
    private TextField animalNumberField;
    @FXML
    private TextField startEnergyTextField;

    @FXML
    private TextField minimumMutationsTextField;
    @FXML
    private TextField maximumMutationsTextField;
    @FXML
    private TextField geneLengthTextField;

    @FXML
    private TextField burningTimeTextField;
    @FXML
    private TextField fireProbabilityTextField;
    @FXML
    private TextField fireDamegeTextField;


    @FXML
    public void startDefaultSimulation() {
        startSimulation(true);
    }

    @FXML
    public void startCustomSimulation() {
        startSimulation(false);
    }

    public void startSimulation(boolean defaultSimulation) {
        SimulationConfig config = getConfigFromUI(defaultSimulation);
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

        stage.show();
        presenter.drawMap(map);

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

    private double getDoubleFromTextField(TextField textField) {
        String text = textField.getText();
        if (text == null || text.trim().isEmpty()) {
            text = textField.getPromptText();
        }

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private SimulationConfig getConfigFromUI(boolean defaultSimulation) {
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
        var fireConfig = new SimulationConfig.Fire(
                !defaultSimulation ? getDoubleFromTextField(fireProbabilityTextField) : 0.0,
                !defaultSimulation ? getIntFromTextField(burningTimeTextField) : 0,
                !defaultSimulation ? getIntFromTextField(fireDamegeTextField) : 0
        );

        return new SimulationConfig(
                mapConfig,
                energyConfig,
                animalConfig,
                genotypeConfig,
                fireConfig
        );
    }
}

