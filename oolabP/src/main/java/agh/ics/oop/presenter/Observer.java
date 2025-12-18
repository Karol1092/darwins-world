package agh.ics.oop.presenter;

import agh.ics.oop.model.world_map.WorldMap;

public interface Observer {
    void mapChanged(WorldMap map, String Message);
}
