package agh.ics.oop.simulations;

import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.Grass;
import agh.ics.oop.model.world.element.WorldDirections;
import agh.ics.oop.model.world.map.WorldMap;
import agh.ics.oop.util.*;

import java.util.*;

import static java.lang.Math.max;

public class Simulation implements Runnable {

    private int days = 0;
    private final List<Animal> animals;
    private final List<Grass> grasses = new ArrayList<>();
    private final WorldMap map;
    private final SimulationConfig config;
    private final AnimalRandomizer randomizer;
    private List<Animal> animalsToRemove = new ArrayList<>();
    private boolean paused = false;
    private boolean running = true;

//    wartości do pokazywania w presenterze:
    private int animalCounter;
    private int grassCounter;
    private int freeSpaceCounter;
    private List<Integer> mostPopularGene;
    private double avgEnergy;
    private double avgLifeSpan;
    private double avgChildCount;

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

        animalsGrassEating();

        List<Animal> newborns = map.animalsReproduction();
        animals.addAll(newborns);

        map.grassPlacement(config.map().numberOfGrassSpawn());

        map.mapChanged("day: " + days + ", " + "number of animals: " + animals.size());

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
        }
        animalsToRemove = new ArrayList<>();
    }

    private void moveAliveAnimals() throws Exception {
        for (Animal animal : animals) {

            if (animal.getLifeEnergy() <= 0) {
                map.removeAnimal(animal);
                animalsToRemove.add(animal);
            }

            map.move(animal, animal.getGene().get(((days - 1) % animal.getGene().size())));
            animal.setLifeEnergy(animal.getLifeEnergy() - config.energy().dailyLoss());
        }
    }

    private void animalsGrassEating() {
        for (Animal animal : animals) {
            Grass grass = map.getGrass(animal.getPosition());
            if (grass != null) {
                map.removeGrass(grass);
                grasses.remove(grass);
                animal.setLifeEnergy(animal.getLifeEnergy() + config.energy().grassProfit());
            }
        }
    }
}
