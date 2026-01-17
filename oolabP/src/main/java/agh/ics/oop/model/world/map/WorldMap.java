package agh.ics.oop.model.world.map;


import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.Grass;
import agh.ics.oop.model.world.element.WorldDirections;
import agh.ics.oop.model.world.element.WorldElement;
import agh.ics.oop.presenter.Observer;
import agh.ics.oop.util.JungleGrassPositionsGenerator;
import agh.ics.oop.util.SimulationConfig;
import agh.ics.oop.util.SteppeGrassPositionsGenerator;
import agh.ics.oop.util.Vector2d;

import java.util.*;

public class WorldMap{
    private final HashMap<Vector2d,List<Animal>> animals = new HashMap<>();
    private final HashMap<Vector2d, Grass> grasses = new HashMap<>();
    private final Vector2d lowerLeft = new Vector2d(0,0);
    private final Vector2d upperRight;
    protected final List<Observer> observers = new ArrayList<>();
    private Iterator<Vector2d> jungleIterator;
    private Iterator<Vector2d> steppeIterator;
    private final Random random = new Random();
    private final int[] jungleToSteppeRatio = {0,0,0,0,1};
    public WorldMap(SimulationConfig config) {
        this.upperRight = new Vector2d((config.map().width() - 1), (config.map().height() - 1));
        int height = config.map().height();
        int width = config.map().width();
        int jungleHeight = Math.max(1, (int)Math.round(height * 0.2));
        int minHeight = (height - jungleHeight) / 2;
        int maxHeight = minHeight + jungleHeight - 1;
        int jungleSize = (maxHeight-minHeight+1)*width;
        int mapSize = width*height;

        this.jungleIterator = new JungleGrassPositionsGenerator(width,
                minHeight, maxHeight, jungleSize).iterator();
        this.steppeIterator = new SteppeGrassPositionsGenerator(width,
                minHeight, maxHeight, height, mapSize - jungleSize).iterator();
    }
    public boolean isOccupied(Vector2d position){
        List<Animal> animalsAtPosition = animals.get(position);
        return (animalsAtPosition != null && !animalsAtPosition.isEmpty())
                || grasses.containsKey(position);
    }

    public Grass getGrass(Vector2d position) {
        return grasses.getOrDefault(position, null);
    }
    public void grassPlacement(int count) {
        int place =0;
        for (int i = 0; i < count; i++) {
            place = random.nextInt(jungleToSteppeRatio.length);
            if (jungleToSteppeRatio[place]==0){
                if (jungleIterator.hasNext()){
                    place(new Grass(jungleIterator.next()));
                }else if (steppeIterator.hasNext()) {
                    place(new Grass(steppeIterator.next()));
                }
            }else if (steppeIterator.hasNext()){
                place(new Grass(steppeIterator.next()));
            }
        }
    }
    public void removeGrass(Grass grass) {
        grasses.remove(grass.getPosition(), grass);
    }

    public List<WorldElement> getElements(Vector2d position) {
        List<WorldElement> elements = new ArrayList<>();
        if (isOccupied(position)) {
            if (grasses.containsKey(position)) elements.add(grasses.get(position));
            List<Animal> animalsAtPosition = animals.get(position);
            if (animalsAtPosition!=null) elements.addAll(animalsAtPosition);
        }
        return elements;
    }

    public List<WorldElement> getAllElements(){
        List<WorldElement> elements = new ArrayList<>();
        for (int i = 0; i<upperRight.getX()+1; i++){
            for (int j = 0; j<upperRight.getY()+1; j++) {
                Vector2d position = new Vector2d(i, j);
                if (isOccupied(position)) {
                    if (grasses.containsKey(position)) elements.add(grasses.get(position));
                    List<Animal> animalsAtPosition = animals.get(position);
                    if (animalsAtPosition != null) elements.addAll(animalsAtPosition);
                }
            }
        }
        return elements;
    }

    public void place(WorldElement element) {
        if (element instanceof Grass) {
            grasses.put(element.getPosition(), (Grass) element);
        }
        else if (element instanceof Animal) {
            animals.computeIfAbsent(element.getPosition(), k  -> new ArrayList<>()).add((Animal) element);
        }
    }

    public void removeAnimal(Animal animal) {
        Vector2d position = animal.getPosition();
        List<Animal> animalsAtPosition = animals.get(position);

        if (animalsAtPosition != null) {
            animalsAtPosition.remove(animal);

            if (animalsAtPosition.isEmpty()) {
                animals.remove(position);
            }
        }
    }

    private int verticalPositionCheck(Animal animal, WorldDirections facingDirection) throws Exception {
        int y = animal.getPosition().getY();
        if (y > upperRight.getY() || y < lowerLeft.getY()) {
            animal.setFacingDirection(WorldDirections.bounce(facingDirection));
            animal.move(0);
            if (animal.getFacingDirection() == WorldDirections.NORTH
                    || animal.getFacingDirection() == WorldDirections.SOUTH) {
                animal.move(0);
            }
        }
        return animal.getPosition().getY();
    }
    public void move(Animal animal,int rotation) throws Exception{
        removeAnimal(animal);
        animal.move(rotation);
        WorldDirections facingDirection = animal.getFacingDirection();
        int y = verticalPositionCheck(animal,facingDirection);
        int range = upperRight.getX()-lowerLeft.getX()+1;
        int x = ((animal.getPosition().getX() - lowerLeft.getX()) % range + range) % range + lowerLeft.getX();

        animal.setPosition(new Vector2d(x,y));
        animals.computeIfAbsent(animal.getPosition(), k -> new ArrayList<>()).add(animal);
        mapChanged("Animal moved at : " + animal.getPosition() + "\n" + "Animal health: " + animal.getLifeEnergy());
    }

    public Vector2d getLowerLeft() {
        return lowerLeft;
    }
    public Vector2d getUpperRight() {
        return  upperRight;
    }
    public void addObserver(Observer observer){
        observers.add(observer);
    }
    public void removeObserver(Observer observer){
        observers.remove(observer);
    }
    public void mapChanged(String message) {
        for (Observer observer : observers) {
            observer.mapChanged(this, message);
        }
    }
}
