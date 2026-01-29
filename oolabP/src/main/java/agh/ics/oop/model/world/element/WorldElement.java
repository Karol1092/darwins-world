package agh.ics.oop.model.world.element;

import agh.ics.oop.util.Vector2d;

public interface WorldElement {
    Vector2d getPosition();
    boolean getIsBurning();
    void setIsBurning(boolean isBurning);
    int getBurning();
    void setBurning(int burning);
}
