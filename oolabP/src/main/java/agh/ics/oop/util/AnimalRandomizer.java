package agh.ics.oop.util;

import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.WorldDirections;

import java.util.ArrayList;
import java.util.Random;

public  class AnimalRandomizer {
    private final Random random = new Random();
    private final WorldDirections[] facingDirectionsPool = {
            WorldDirections.NORTH,
            WorldDirections.NORTH_EAST,
            WorldDirections.EAST,
            WorldDirections.SOUTH_EAST,
            WorldDirections.SOUTH,
            WorldDirections.SOUTH_WEST,
            WorldDirections.WEST,
            WorldDirections.NORTH_WEST};
    private final ArrayList<Animal> animals =  new ArrayList<>();

    public  ArrayList<Animal>randomizer(SimulationConfig config) {
        for (int i = 0; i < config.animal().numberAtStart(); i++) {
            ArrayList<Integer> gene = new ArrayList<>();
            for (int j = 0; j < config.genotype().length(); j++) {
                gene.add(random.nextInt(8));
            }
            animals.add(new Animal(
                    new Vector2d(random.nextInt(0, config.map().width()),random.nextInt(0,config.map().height())),
                    facingDirectionsPool[random.nextInt(facingDirectionsPool.length)],
                    gene,
                    config.animal().energyAtStart()));
        }
        return animals;
    }
}
