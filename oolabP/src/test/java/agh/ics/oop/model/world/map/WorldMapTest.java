package agh.ics.oop.model.world.map;

import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.Grass;
import agh.ics.oop.model.world.element.WorldDirections;
import agh.ics.oop.util.SimulationConfig;
import agh.ics.oop.util.Vector2d;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorldMapTest {
    SimulationConfig config = new SimulationConfig(
            new SimulationConfig.Map(5, 5, 0, 0),
            new SimulationConfig.Energy(5, 1, 10, 3),
            new SimulationConfig.Animal(0, 20),
            new SimulationConfig.Genotype(1, 3, 8),
            new SimulationConfig.Fire(0.0, 0, 0)
    );
    WorldMap map = new WorldMap(config);



    @Test
    void removeGrass() {
        Grass grass = new Grass (new Vector2d(2,2));
        map.place(grass);
        map.removeGrass(grass);
        assertFalse(map.isOccupied(new Vector2d(2,2)));

    }

    @Test
    void place() {
        Grass grass1 = new Grass(new Vector2d(2, 2));
        Grass grass2 = new Grass(new Vector2d(3, 3));
        Grass grass3 = new Grass(new Vector2d(5, 5));
        Animal animal1 = new Animal(new Vector2d(1, 1));
        Animal animal2 = new Animal(new Vector2d(-1, -1));
        map.place(grass1);
        map.place(grass2);
        map.place(grass3);
        map.place(animal1);
        map.place(animal2);
        assertEquals(map.getAllElements(), List.of(animal1, grass1, grass2));
    }


    @Test
    void removeAnimal() {
        Animal animal1 = new Animal(new Vector2d(1, 1));
        map.place(animal1);
        map.removeAnimal(animal1);
        assertFalse(map.isOccupied(new Vector2d(1,1)));
    }

    @Test
    void move() {
        Animal animal = new Animal(new Vector2d(0,1));
        Animal animal1 = new Animal(new Vector2d(2,4));
        Animal animal2 = new Animal(new Vector2d(4,2));
        map.place(animal);
        map.place(animal1);
        map.place(animal2);
        animal.setFacingDirection(WorldDirections.SOUTH_WEST);
        animal1.setFacingDirection(WorldDirections.NORTH);
        animal2.setFacingDirection(WorldDirections.EAST);
        try {
            map.move(animal,0);
            map.move(animal1,0);
            map.move(animal2,0);
        }
        catch (Exception e){
            IO.print(e.getMessage());
        }
        assertEquals(new Vector2d(4,0),animal.getPosition());
        assertEquals(new Vector2d(2,3),animal1.getPosition());
        assertEquals(new Vector2d(0,2),animal2.getPosition());


    }
}