package agh.ics.oop.util;

import agh.ics.oop.model.world.element.Animal;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record SimulationState(
        int day,
        Map<Vector2d, List<AnimalConfig>> animalsPositions,
        Set<Vector2d> grassPositions,
        String statistics
) {}
