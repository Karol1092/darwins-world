package agh.ics.oop;

import agh.ics.oop.model.world_element.Animal;
import agh.ics.oop.model.world_element.WorldDirections;
import agh.ics.oop.model.world_map.WorldMap;
import agh.ics.oop.simulations.Simulation;
import agh.ics.oop.util.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class World {
    public static void main(String[] args) {
        WorldMap map = new WorldMap(5,5);
        List<Integer> gene = new  ArrayList<>(List.of(0,0));
        List<Animal> animals = new ArrayList<>(List.of(new Animal(new Vector2d(2, 2), WorldDirections.NORTH_EAST,gene),
                new Animal(new Vector2d(2, 2), WorldDirections.NORTH_EAST,gene)));
        Simulation simulation = new Simulation(map,animals);
        try{
            simulation.run();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
