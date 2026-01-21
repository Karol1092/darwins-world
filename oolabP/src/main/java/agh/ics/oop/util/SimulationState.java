package agh.ics.oop.util;

import javafx.scene.chart.XYChart;
import java.util.List;
import java.util.Map;

public record SimulationState(
        int day,
        int jungleSize,
        Map<Vector2d, List<AnimalConfig>> animalsPositions,
        Map<Vector2d, Boolean> grassPositions,
        String statistics,
        Map<Vector2d,Integer> popularGrassPositions
) {}
