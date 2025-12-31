package agh.ics.oop.model.world_map;


import agh.ics.oop.model.world_element.Animal;
import agh.ics.oop.model.world_element.Grass;
import agh.ics.oop.model.world_element.WorldDirections;
import agh.ics.oop.model.world_element.WorldElement;
import agh.ics.oop.presenter.Observer;
import agh.ics.oop.util.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorldMap{
    private final HashMap<Vector2d,List<Animal>> animals = new HashMap<>();
    private final HashMap<Vector2d, Grass> grasses = new HashMap<>();
    private final Vector2d lowerLeft = new Vector2d(0,0);
    private final Vector2d upperRight;
    protected final List<Observer> observers = new ArrayList<>();

    public WorldMap(int width, int height) {
        this.upperRight = new Vector2d(width-1, height-1);
    }

    public boolean isOccupied(Vector2d position){
        List<Animal> animalsAtPosition = animals.get(position);
        return (animalsAtPosition != null && !animalsAtPosition.isEmpty())
                || grasses.containsKey(position);
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
        animals.remove(animal.getPosition());
        animal.move(rotation);
        WorldDirections facingDirection = animal.getFacingDirection();
        int y = verticalPositionCheck(animal,facingDirection);
        int range = upperRight.getX()-lowerLeft.getX()+1;
        int x = ((animal.getPosition().getX() - lowerLeft.getX()) % range + range) % range + lowerLeft.getX();

        animal.setPosition(new Vector2d(x,y));
        mapChanged("Animal moved at : " + animal.getPosition());
        animals.computeIfAbsent(animal.getPosition(), k -> new ArrayList<>()).add(animal);
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
