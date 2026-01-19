package agh.ics.oop.simulations;

import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.Grass;
import agh.ics.oop.model.world.element.WorldDirections;
import agh.ics.oop.model.world.map.WorldMap;
import agh.ics.oop.util.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;

public class Simulation implements Runnable {

    private int days = 0;
    private final List<Animal> animals;
    private final WorldMap map;
    private final SimulationConfig config;
    private final AnimalRandomizer randomizer;
    private List<Animal> animalsToRemove = new ArrayList<>();
    private List<Integer> deadAnimalsLifespans = new ArrayList<>();
    private boolean paused = false;
    private boolean running = true;

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

        updateStatistics();

        try{
            Thread.sleep(1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void removeDeadAnimals() {
        for(Animal animalToRemove : animalsToRemove){
            animals.remove(animalToRemove);
            map.removeAnimal(animalToRemove);
            deadAnimalsLifespans.add(animalToRemove.getAge());
        }
        animalsToRemove = new ArrayList<>();
    }

    private void moveAliveAnimals() throws Exception {
        for (Animal animal : animals) {

            if (animal.getLifeEnergy() <= 0) {
                map.removeAnimal(animal);
                animalsToRemove.add(animal);
            }
            else {
                map.move(animal, animal.getGene().get(((days - 1) % animal.getGene().size())));
                animal.setLifeEnergy(animal.getLifeEnergy() - config.energy().dailyLoss());
            }
        }
    }

    private void updateStatistics() {
        int animalCounter = animals.size();
        int grassCounter = map.getAllGrasses().size();
        int freeSpaceCounter = config.map().height() * config.map().width() - map.getAllElementsPositions().size();
        String mostPopularGenotype = getMostPopularGenotype();
        double averageEnergy = getAverageEnergy();
        double averageLifespan = getAverageLifespan();
        double averageNumberOfChildren = getAverageNumberOfChildren();

        map.mapChanged("Day: " + days + "\n" +
                "Number of animals: " + animalCounter + "\n" +
                "Number of grass: " + grassCounter + "\n" +
                "Number of free tiles: " + freeSpaceCounter + "\n" +
                "The most popular genotype: " + mostPopularGenotype + "\n" +
                "Average energy: " + averageEnergy + "\n" +
                "Average lifespan: " + averageLifespan + "\n" +
                "Average number of children: " + averageNumberOfChildren);

    }

    private String getMostPopularGenotype() {
        Map<List<Integer>, Integer> genotypeCounter = new HashMap<>();

        for(Animal animal : animals) {
            List<Integer> genotype = animal.getGene();
            genotypeCounter.put(genotype, genotypeCounter.getOrDefault(genotype, 0) + 1);
        }

        List<Integer> mostPopular = null;
        int maxCounter = -1;

        for(Map.Entry<List<Integer>, Integer> entry : genotypeCounter.entrySet()) {
            if (entry.getValue() > maxCounter) {
                maxCounter = entry.getValue();
                mostPopular = entry.getKey();
            }
        }

        if (mostPopular != null) {
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
}
