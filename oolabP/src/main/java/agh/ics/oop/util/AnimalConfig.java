package agh.ics.oop.util;

import agh.ics.oop.model.world.element.WorldDirections;

public record AnimalConfig(
    Vector2d position,
    WorldDirections facingDirection,
    int lifeEnergy,
    boolean isBurning,
    int age,
    int numberOfChildren
) {}
