package agh.ics.oop.util;

import java.util.*;

public class SteppeGrassPositionsGenerator implements Iterable<Vector2d> {


    private final int count;
    private final List<Vector2d> indexes = new ArrayList<>();

    public SteppeGrassPositionsGenerator(int width,int jungleLowerIndex,int jungleUpperIndex, int height, int count) {

        this.count = count;

        for (int i = 0; i < height; i++) {
            if (i<jungleLowerIndex || i > jungleUpperIndex) {
                for (int j = 0; j < width; j++) {
                    indexes.add(new Vector2d(j, i));
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

