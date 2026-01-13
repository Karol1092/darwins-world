package agh.ics.oop.simulations;

import agh.ics.oop.model.world_element.Animal;
import agh.ics.oop.model.world_element.Grass;
import agh.ics.oop.model.world_map.WorldMap;
import agh.ics.oop.util.JungleGrassPositionsGenerator;
import agh.ics.oop.util.SteppeGrassPositionsGenerator;
import agh.ics.oop.util.Vector2d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Simulation {

    private int days = 0;
    private final List<Animal> animals;
    private final List<Grass> grasses = new ArrayList<>();
    private final WorldMap map;
    private Iterator<Vector2d> jungleIterator;
    private  Iterator<Vector2d> steppeIterator;
    private final int noOfGrass;
    private  final int[] jungleToSteppeRatio = {0,0,0,0,1};
    private final Random random = new Random();

    public Simulation(WorldMap map, List<Animal> animals, int noOfGrass) {
        this.animals = animals;
        this.map = map;
        this.noOfGrass = noOfGrass;
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
        int place =0;
        for (int i = 0; i < noOfGrass; i++) {
            place = random.nextInt(jungleToSteppeRatio.length);
            if (jungleToSteppeRatio[place]==0){

                if (jungleIterator.hasNext()){
                    map.place(new Grass(jungleIterator.next()));
                    IO.println("jungle");
                }else if (steppeIterator.hasNext()) {
                    map.place(new Grass(steppeIterator.next()));
                    IO.println("steppe");
                }
            }else if (steppeIterator.hasNext()){
                map.place(new Grass(steppeIterator.next()));
                IO.println("steppe");
            }
        }
//      zwierzęta startowe:
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
//      Animals moves

        for (Animal animal : animals) {
            map.move(animal, animal.getGene().get(((days - 1) % animal.getGene().size())));

            try{
                Thread.sleep(500);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
//      Grass Growth
        int place =0;
        for (int i = 0; i < noOfGrass; i++) {
            place = random.nextInt(jungleToSteppeRatio.length);
            if (jungleToSteppeRatio[place]==0){

                if (jungleIterator.hasNext()){
                    map.place(new Grass(jungleIterator.next()));
                    IO.println("jungle");
                }else if (steppeIterator.hasNext()) {
                    map.place(new Grass(steppeIterator.next()));
                    IO.println("steppe");
                }
            }else if (steppeIterator.hasNext()){
                map.place(new Grass(steppeIterator.next()));
                IO.println("steppe");
            }
        }
    }
}
