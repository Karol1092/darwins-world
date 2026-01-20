package agh.ics.oop.util;

import agh.ics.oop.model.world.element.Animal;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record SimulationState(
        int day,
        Map<Vector2d, List<AnimalConfig>> animalsPositions,
        Map<Vector2d, Boolean> grassPositions,
        String statistics
) {}
