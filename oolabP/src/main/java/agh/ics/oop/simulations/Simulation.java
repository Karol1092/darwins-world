package agh.ics.oop.simulations;

import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.Grass;
import agh.ics.oop.model.world.element.WorldDirections;
import agh.ics.oop.model.world.map.WorldMap;
import agh.ics.oop.util.*;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.max;

public class Simulation implements Runnable {

    private int days = 0;
    private final List<Animal> animals;
    private final List<Grass> grasses = new ArrayList<>();
    private final WorldMap map;
    private final SimulationConfig config;
    private final AnimalRandomizer randomizer;
    private List<Animal> animalsToRemove = new ArrayList<>();

    public Simulation(SimulationConfig config) {
        this.randomizer = new AnimalRandomizer();
        this.animals = randomizer.randomizer(config);
        this.map = new WorldMap(config);
        this.config = config;
//      trawa startowa:
        map.grassPlacement(config.map().numberOfGrass());
//      zwierzęta startowe:
        for (Animal animal : animals) {
            map.place(animal);
        }
    }

    public WorldMap getWorldMap() {
        return this.map;
    }

    public void run() {
        for (int i =0; i<8; i++) {
            days++;
            try {
                dayCycle();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void dayCycle() throws Exception {
        removeDeadAnimals();
        moveAliveAnimals();
        animalsGrassEating();
        map.grassPlacement(config.map().numberOfGrassSpawn());
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
            animal.setLifeEnergy(animal.getLifeEnergy() - 10);

            try{
                Thread.sleep(500);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private void animalsGrassEating() {
        for (Animal animal : animals) {
            Grass grass = map.getGrass(animal.getPosition());
            if (grass != null) {
                map.removeGrass(grass);
                grasses.remove(grass);
                animal.setLifeEnergy(animal.getLifeEnergy() + 30);
            }
        }
    }

}
