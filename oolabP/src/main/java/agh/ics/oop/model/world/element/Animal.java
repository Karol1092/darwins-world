package agh.ics.oop.model.world.element;

import agh.ics.oop.util.Vector2d;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Animal implements WorldElement {

    private  Vector2d position;
    private WorldDirections facingDirection;
    private final List<Integer> gene;
    private int lifeEnergy;
    private boolean isBurning = false;
    private int burning;
    private int age;
    private int numberOfChildren;
    private boolean isMostPopularGene;
    private final UUID id = UUID.randomUUID();

    public Animal(Vector2d position) {
        this.position = position;
        this.facingDirection = WorldDirections.NORTH;
        this.gene = new ArrayList<>(List.of(0,0,0,0,0,0,1));
        this.lifeEnergy = 100;
        this.age = 0;
        this.numberOfChildren = 0;
        this.isMostPopularGene = false;
    }

    public Animal(Vector2d position, WorldDirections facingDirection, List<Integer> gene, int lifeEnergy) {
        this.gene = gene;
        this.position = position;
        this.facingDirection = facingDirection;
        this.lifeEnergy = lifeEnergy;
        this.age = 0;
        this.numberOfChildren = 0;
        this.isMostPopularGene = false;
    }

    public Animal(Vector2d position, List<Integer> gene) {
        this.gene = gene;
        this.position = position;
        this.facingDirection = WorldDirections.NORTH;
        this.age = 0;
        this.numberOfChildren = 0;
        this.isMostPopularGene = false;
    }
    @Override
    public boolean getIsBurning() {
        return isBurning;
    }

    @Override
    public void setIsBurning(boolean burning) {
        this.isBurning = burning;
    }

    @Override
    public int getBurning() {
        return this.burning;
    }

    @Override
    public void setBurning(int burning) {
        this.burning = burning;
    }

    @Override
    public Vector2d getPosition() {
        return  position;
    }

    public void setLifeEnergy(int lifeEnergy) {
        this.lifeEnergy = lifeEnergy;
    }

    public int getLifeEnergy(){
        return lifeEnergy;
    }

    public int getAge() {
        return age;
    }

    public UUID getUniqueId() {
        return id;
    }

    public int getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(int newNumberOfChildren) {
        numberOfChildren = newNumberOfChildren;
    }

    public void setPosition(Vector2d position) {
        this.position = position;
    }

    public WorldDirections getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(WorldDirections facingDirection) {
        this.facingDirection = facingDirection;
    }

    public List<Integer> getGene() {
        return gene;
    }

    public boolean isMostPopularGene() {
        return isMostPopularGene;
    }

    public void setMostPopularGene(boolean flag) {
        this.isMostPopularGene = flag;
    }

    public void move(int rotation) throws Exception {
        switch (rotation){
            case 0 -> facingDirection = facingDirection;
            case 1 -> facingDirection = WorldDirections.next(facingDirection);
            case 2 -> facingDirection = WorldDirections.next(WorldDirections.next(facingDirection));
            case 3 -> facingDirection = WorldDirections.previous(WorldDirections.opposite(facingDirection));
            case 4 -> facingDirection = WorldDirections.opposite(facingDirection);
            case 5 -> facingDirection = WorldDirections.next(WorldDirections.opposite(facingDirection));
            case 6 -> facingDirection = WorldDirections.previous(WorldDirections.previous(facingDirection));
            case 7 -> facingDirection = WorldDirections.previous(facingDirection);
            default -> throw new Exception("Wrong rotation in Animal Genes");
        }
        position = position.add(WorldDirections.toUnitVector(facingDirection));
        age++;
    }

    @Override
    public String toString() {
        return switch (facingDirection) {
            case NORTH,NORTH_EAST,NORTH_WEST -> "^";
            case SOUTH,SOUTH_EAST,SOUTH_WEST -> "v";
            case WEST -> "<";
            case EAST -> ">";
        };
    }
}
