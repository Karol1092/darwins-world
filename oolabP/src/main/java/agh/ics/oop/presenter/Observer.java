package agh.ics.oop.presenter;

import agh.ics.oop.model.world.map.WorldMap;
import javafx.scene.chart.Chart;

public interface Observer {
    void mapChanged(WorldMap map, String Message);
}
