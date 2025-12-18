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
        WorldMap map = new WorldMap(10,5);
        List<Integer> gene = new  ArrayList<>(List.of(0,0));
        List<Animal> animals = new ArrayList<>(List.of(new Animal(new Vector2d(2,1)),new Animal(new Vector2d(3,4))));
        Simulation simulation = new Simulation(map,animals,5);
        try{
            simulation.run();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
