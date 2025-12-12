package agh.ics.oop.simulations;

import agh.ics.oop.model.world_element.Animal;
import agh.ics.oop.model.world_element.Grass;
import agh.ics.oop.model.world_map.WorldMap;
import agh.ics.oop.util.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class Simulation {
    private int days = 0;
    private final List<Animal> animals;
    private final List<Grass> grasses = new ArrayList<>();
    private final WorldMap map;

    public Simulation(WorldMap map, List<Animal> animals) {
        this.animals = animals;
        this.map = map;
        for (Animal animal : animals) {
            map.place(animal);
        }
    }
    public void run() throws Exception{
        for (int i =0; i<10; i++) {
            days++;
            IO.println(days);
            daycycle();
        }
    }
    public void daycycle() throws Exception {
        for (Animal animal : animals){
            map.move(animal,animal.getGene().get(((days-1)%animal.getGene().size())));
            IO.println(animal.getPosition());
            IO.print(animal.getFacingDirection() + "\n");
        }
    }
}
