package agh.ics.oop.model.world.element;

import agh.ics.oop.util.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class Animal implements WorldElement {

    private  Vector2d position;
    private WorldDirections facingDirection;
    private final List<Integer> gene;
    private int lifeEnergy;
    private boolean isBurning = false;
    private int burning;
    public Animal(Vector2d position) {
        this.position = position;
        this.facingDirection = WorldDirections.NORTH;
        this.gene = new ArrayList<>(List.of(0,0,0,0,0,0,1));
        this.lifeEnergy = 100;
    }
    public Animal(Vector2d position, WorldDirections facingDirection, List<Integer> gene, int lifeEnergy) {
        this.gene = gene;
        this.position = position;
        this.facingDirection = facingDirection;
        this.lifeEnergy = lifeEnergy;
    }
    public Animal(Vector2d position, List<Integer> gene) {
        this.gene = gene;
        this.position = position;
        this.facingDirection = WorldDirections.NORTH;
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

    public void setLifeEnergy(int lifeEnergy) {
        this.lifeEnergy = lifeEnergy;
    }

    public int getLifeEnergy(){
        return lifeEnergy;
    }

    @Override
    public Vector2d getPosition() {
        return  position;
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

    public void move(int rotation) throws Exception {
        switch (rotation){
            case 0 -> facingDirection = facingDirection;  //trzeba to podmienić na nic nie robienie
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
