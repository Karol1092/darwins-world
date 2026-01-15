package agh.ics.oop.simulations;

import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.Grass;
import agh.ics.oop.model.world.element.WorldDirections;
import agh.ics.oop.model.world.map.WorldMap;
import agh.ics.oop.util.JungleGrassPositionsGenerator;
import agh.ics.oop.util.SimulationConfig;
import agh.ics.oop.util.SteppeGrassPositionsGenerator;
import agh.ics.oop.util.Vector2d;

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
    private Iterator<Vector2d> jungleIterator;
    private Iterator<Vector2d> steppeIterator;
    private final int noOfGrass;
    private final int[] jungleToSteppeRatio = {0,0,0,0,1};
    private final Random random = new Random();
    private List<Animal> animalsToRemove = new ArrayList<>();

    public Simulation(SimulationConfig config) {
        this.animals = new ArrayList<>(
                List.of(
                        new Animal(new Vector2d(4, 3), WorldDirections.NORTH, new ArrayList<>(List.of(0, 0, 0, 0, 0, 0, 0, 1)), config.animal().energyAtStart()),
                        new Animal(new Vector2d(1, 9), WorldDirections.SOUTH, new ArrayList<>(List.of(0, 0, 0, 0, 0, 0, 0, 0)), config.animal().energyAtStart())
                ));

        this.map = new WorldMap(config.map().width(), config.map().height());
        this.noOfGrass = config.map().numberOfGrassSpawn();
//      wymiary mapy:
        int height = map.getUpperRight().getY()+1;
        int width = map.getUpperRight().getX()+1;
        int mapSize = height * width;
//      wymiary jungli:
        int jungleHeight = Math.max(1, (int)Math.round(height * 0.2));
        int minHeight = (height - jungleHeight) / 2;
        int maxHeight = minHeight + jungleHeight - 1;
        int jungleSize = (maxHeight-minHeight+1)*width;
//      generatory trawy:
        this.jungleIterator = new JungleGrassPositionsGenerator(width,
                minHeight, maxHeight,jungleSize).iterator();
        this.steppeIterator  = new SteppeGrassPositionsGenerator(width,
                minHeight,maxHeight,height,mapSize-jungleSize).iterator();
//      trawa startowa:
        grassPlacement();
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
                daycycle();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void daycycle() throws Exception {
        removeDeadAnimals();
        moveAliveAnimals();
        animalsGrassEating();
        grassPlacement();
        }

    private void grassPlacement() {
        int place =0;
        for (int i = 0; i < noOfGrass; i++) {
            place = random.nextInt(jungleToSteppeRatio.length);
            if (jungleToSteppeRatio[place]==0){
                if (jungleIterator.hasNext()){
                    map.place(new Grass(jungleIterator.next()));
                }else if (steppeIterator.hasNext()) {
                    map.place(new Grass(steppeIterator.next()));
                }
            }else if (steppeIterator.hasNext()){
                map.place(new Grass(steppeIterator.next()));
            }
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
