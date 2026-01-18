package agh.ics.oop.util;

import agh.ics.oop.util.Vector2d;

import java.util.*;

public class JungleGrassPositionsGenerator implements Iterable<Vector2d> {


    private final int count;
    private final List<Vector2d> indexes = new ArrayList<>();
    public JungleGrassPositionsGenerator(int width,int minHeight,int maxHeight,int count) {
        this.count = count;

        {
            for (int i = 0; i < width; i++) {
                for (int j = minHeight; j < (maxHeight+1); j++) {
                    indexes.add(new Vector2d(i, j));
                }
            }
        }
    }
    public void addIndex(Vector2d index) {
        this.indexes.add(index);
    }

    @Override
    public Iterator<Vector2d> iterator() {

        return new Iterator<>() {
            private final Random rand = new Random();
            private int returned = 0;
            @Override
            public Vector2d next() {
                int newPosition = rand.nextInt(indexes.size());
                Vector2d element = indexes.get(newPosition);
                indexes.set(newPosition,indexes.getLast());
                indexes.removeLast();
                returned ++;
                return element;
            }
            @Override
            public boolean hasNext() {
                return returned < count && !indexes.isEmpty();
            }
        };
    }
}

