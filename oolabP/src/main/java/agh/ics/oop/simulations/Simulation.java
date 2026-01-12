package agh.ics.oop.simulations;

import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.Grass;
import agh.ics.oop.model.world.map.WorldMap;
import agh.ics.oop.util.JungleGrassPositionsGenerator;
import agh.ics.oop.util.SteppeGrassPositionsGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulation {

    private int days = 0;
    private final List<Animal> animals;
    private final List<Grass> grasses = new ArrayList<>();
    private final WorldMap map;
    private JungleGrassPositionsGenerator jungleGrassPositionsGenerator;
    private SteppeGrassPositionsGenerator steppeGrassPositionsGenerator;
    private final int noOfGrass;
    private final int[] jungleToSteppeRatio = {0,0,0,0,1};
    private final Random random = new Random();

    public Simulation(WorldMap map, List<Animal> animals, int noOfGrass) {
        this.animals = animals;
        this.map = map;
        this.noOfGrass = noOfGrass;
        int jungleSize = (map.getUpperRight().getX()+1) * ((int) ((map.getUpperRight().getY()+1)*0.6) - (int) ((map.getUpperRight().getY()+1)*0.4)+1);
        int mapSize = (map.getUpperRight().getX()+1) * (map.getUpperRight().getY()+1);
        this.jungleGrassPositionsGenerator = new JungleGrassPositionsGenerator(map.getUpperRight().getX()+1,(int) ((map.getUpperRight().getY()+1)*0.4), (int) ((map.getUpperRight().getY()+1)*0.6),jungleSize);
        this.steppeGrassPositionsGenerator  = new SteppeGrassPositionsGenerator(map.getUpperRight().getX()+1,map.getUpperRight().getY(),mapSize-jungleSize);
        int place = 0;

        for (int i = 0; i < noOfGrass; i++) {
            place = random.nextInt(jungleToSteppeRatio.length);
            if (jungleToSteppeRatio[place]==0){
                if (jungleGrassPositionsGenerator.iterator().hasNext())
                    map.place(new Grass(jungleGrassPositionsGenerator.iterator().next()));
            }else if (steppeGrassPositionsGenerator.iterator().hasNext()){
                map.place(new Grass(steppeGrassPositionsGenerator.iterator().next()));
            }
        }
        for (Animal animal : animals) {
            map.place(animal);
        }
    }
    public void run() throws Exception{
        for (int i =0; i<8; i++) {
            days++;
            IO.println(days);
            daycycle();
        }
    }
    public void daycycle() throws Exception {
//        Animals moves

        for (Animal animal : animals) {
            map.move(animal, animal.getGene().get(((days - 1) % animal.getGene().size())));

            try{
                Thread.sleep(500);
            }catch (InterruptedException e){
                e.printStackTrace();
            }


            IO.println(animal.getPosition());
            IO.print(animal.getFacingDirection() + "\n");
        }
//        Grass Growth
        int place = 0;
        for (int i = 0; i < noOfGrass; i++) {
            place = random.nextInt(jungleToSteppeRatio.length);
            if (jungleToSteppeRatio[place]==0){
                if (jungleGrassPositionsGenerator.iterator().hasNext())
                    map.place(new Grass(jungleGrassPositionsGenerator.iterator().next()));
            }else if (steppeGrassPositionsGenerator.iterator().hasNext()){
                map.place(new Grass(steppeGrassPositionsGenerator.iterator().next()));
            }
        }
//        Grass test

//        for (int i =0;i<map.getUpperRight().getX();i++) {
//            for (int j = 0; j < map.getUpperRight().getY(); j++) {
//                if (map.isOccupied(new Vector2d(i, j)) && map.getElements(new Vector2d(i, j)).getFirst() instanceof Grass) {
//                    IO.println("Grass at: " + new Vector2d(i, j));
//                }
//            }
//        }
    }
}
