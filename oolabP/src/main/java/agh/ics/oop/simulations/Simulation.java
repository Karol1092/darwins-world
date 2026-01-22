package agh.ics.oop.simulations;

import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.Grass;
import agh.ics.oop.model.world.element.WorldDirections;
import agh.ics.oop.model.world.map.WorldMap;
import agh.ics.oop.util.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;

public class Simulation implements Runnable {

    private int days = 0;
    private final List<Animal> animals;
    private final WorldMap map;
    private final SimulationConfig config;
    private final AnimalRandomizer randomizer;
    private final List<Animal> animalsToRemove = new ArrayList<>();
    private final List<Integer> deadAnimalsLifespans = new ArrayList<>();
    private boolean paused = false;
    private boolean running = true;
    private final List<SimulationState> history = new ArrayList<>();
    private final UUID id = UUID.randomUUID();
    private final String statisticsPath;
    private final BufferedWriter writer;

    public Simulation(SimulationConfig config) {
        this.randomizer = new AnimalRandomizer();
        this.animals = randomizer.randomizer(config);
        this.map = new WorldMap(config);
        this.config = config;
        this.paused = false;
        this.running = true;

//      trawa startowa:
        map.grassPlacement(config.map().numberOfGrass());
//      zwierzęta startowe:
        for (Animal animal : animals) {
            map.place(animal);
        }

        this.statisticsPath = "output/" + id.toString() + ".csv";
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("output"));
            this.writer = new BufferedWriter(new FileWriter(statisticsPath));
            writer.write("Day, Number of animals, Number of Grass, Number of free tiles, The most popular genotype, Average energy, Average lifespan, Average number of children");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void togglePause() {
        this.paused = !this.paused;
        if (!paused) {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public WorldMap getWorldMap() {
        return this.map;
    }

    public List<SimulationState> getHistory() {
        synchronized (history) {
            return List.copyOf(history);
        }
    }

    public void run() {
        while (running) {
            try {
                synchronized (this) {
                    while (paused) {
                        wait();
                    }
                }

                days++;
                dayCycle();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void dayCycle() throws Exception {
        removeDeadAnimals();

        moveAliveAnimals();

        map.animalsGrassEating();

        map.fireDamage();
        map.fireSpreading();
        map.reloadGenerators();

        List<Animal> newborns = map.animalsReproduction();
        animals.addAll(newborns);

        map.grassPlacement(config.map().numberOfGrassSpawn());

        saveState();

        String statistics = this.getStatistics();
        map.mapChanged(statistics);

        writeStatisticsToFile(writer);
        writer.flush();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void removeDeadAnimals() {
        for (Animal animalToRemove : animalsToRemove) {
            animals.remove(animalToRemove);
            map.removeAnimal(animalToRemove);
            deadAnimalsLifespans.add(animalToRemove.getAge());
        }
        animalsToRemove.clear();
    }

    private void moveAliveAnimals() throws Exception {
        for (Animal animal : animals) {

            if (animal.getLifeEnergy() <= 0) {
                map.removeAnimal(animal);
                animalsToRemove.add(animal);
            } else {
                map.move(animal, animal.getGene().get(((days - 1) % animal.getGene().size())));
                animal.setLifeEnergy(Math.max(0, animal.getLifeEnergy() - config.energy().dailyLoss()));
            }
        }
    }

    public String getStatistics() {
        int animalCounter = animals.size();
        int grassCounter = map.getAllGrasses().size();
        int freeSpaceCounter = config.map().height() * config.map().width() - map.getAllElementsPositions().size();
        String mostPopularGenotype = getMostPopularGenotype();
        double averageEnergy = getAverageEnergy();
        double averageLifespan = getAverageLifespan();
        double averageNumberOfChildren = getAverageNumberOfChildren();
        getWorldMap().setStats(new ArrayList<>(List.of(
                (double) days,
                (double) animalCounter,
                (double) grassCounter,
                (double) freeSpaceCounter,
                averageEnergy,
                averageLifespan,
                averageNumberOfChildren)));
        return "Day: " + days + "\n" +
                "Number of animals: " + animalCounter + "\n" +
                "Number of grass: " + grassCounter + "\n" +
                "Number of free tiles: " + freeSpaceCounter + "\n" +
                "The most popular genotype: " + mostPopularGenotype + "\n" +
                "Average energy: " + averageEnergy + "\n" +
                "Average lifespan: " + averageLifespan + "\n" +
                "Average number of children: " + averageNumberOfChildren;
    }

    private String getMostPopularGenotype() {
        Map<List<Integer>, Integer> genotypeCounter = new HashMap<>();

        for (Animal animal : animals) {
            List<Integer> genotype = animal.getGene();
            genotypeCounter.put(genotype, genotypeCounter.getOrDefault(genotype, 0) + 1);
        }

        List<Integer> mostPopular = null;
        int maxCounter = -1;

        for (Map.Entry<List<Integer>, Integer> entry : genotypeCounter.entrySet()) {
            if (entry.getValue() > maxCounter) {
                maxCounter = entry.getValue();
                mostPopular = entry.getKey();
            }
        }

        if (mostPopular != null) {
            for (Animal animal : animals) {
                if (animal.getGene().equals(mostPopular)) {
                    animal.setMostPopularGene(true);
                } else {
                    animal.setMostPopularGene(false);
                }
            }

            return mostPopular.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining());
        } else {
            return "";
        }
    }

    private double getAverageEnergy() {
        double averageEnergy = animals.stream()
                .mapToDouble(Animal::getLifeEnergy)
                .average()
                .orElse(0.0);
        return Math.round(averageEnergy * 100.0) / 100.0;
    }

    private double getAverageLifespan() {
        double averageLifespan = deadAnimalsLifespans.stream()
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);
        return Math.round(averageLifespan * 100.0) / 100.0;
    }

    private double getAverageNumberOfChildren() {
        double averageNumberOfChildren = animals.stream()
                .mapToDouble(Animal::getNumberOfChildren)
                .average()
                .orElse(0.0);
        return Math.round(averageNumberOfChildren * 100.0) / 100.0;
    }

    private void saveState() {
        Map<Vector2d, List<AnimalConfig>> currentAnimals = new HashMap<>();

        for (Animal currentAnimal : animals) {
            if (animalsToRemove.contains(currentAnimal)) continue;
            AnimalConfig config = new AnimalConfig(
                    currentAnimal.getPosition(),
                    currentAnimal.getFacingDirection(),
                    currentAnimal.getLifeEnergy(),
                    currentAnimal.getIsBurning(),
                    currentAnimal.getAge(),
                    currentAnimal.getNumberOfChildren(),
                    currentAnimal.isMostPopularGene()
            );

            currentAnimals
                    .computeIfAbsent(currentAnimal.getPosition(), k -> new ArrayList<>())
                    .add(config);
        }

        Map<Vector2d, Boolean> currentGrasses = new HashMap<>();

        for (Grass currentGrass : map.getAllGrasses()) {
            currentGrasses.put(currentGrass.getPosition(), currentGrass.getIsBurning());
        }
        Map<Vector2d, Integer> popularGrassPositions = new HashMap<>(getWorldMap().getPopularGrassPositions());
        if (!paused) {
            synchronized (history) {
                history.add(new SimulationState(
                        days,
                        getWorldMap().getJungleSize(),
                        currentAnimals,
                        currentGrasses,
                        getStatistics(),
                        popularGrassPositions

                ));
            }
        }
    }

    private void writeStatisticsToFile(BufferedWriter writer) throws IOException {
        int animalCounter = animals.size();
        int grassCounter = map.getAllGrasses().size();
        int freeSpaceCounter = config.map().height() * config.map().width() - map.getAllElementsPositions().size();
        String mostPopularGenotype = getMostPopularGenotype();
        double averageEnergy = getAverageEnergy();
        double averageLifespan = getAverageLifespan();
        double averageNumberOfChildren = getAverageNumberOfChildren();

        String statistics = String.format(Locale.US, "%d, %d, %d, %d, %s, %.2f, %.2f, %.2f",
                days,
                animalCounter,
                grassCounter,
                freeSpaceCounter,
                mostPopularGenotype,
                averageEnergy,
                averageLifespan,
                averageNumberOfChildren);

        writer.newLine();
        writer.write(statistics);
    }
}