package agh.ics.oop.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SteppeGrassPositionsGenerator implements Iterable<Vector2d> {

    private final int width;
    private final int height;
    private final int count;
    private final int jungleUpperIndex;
    private final int jungleLowerIndex;


    public SteppeGrassPositionsGenerator(int width,int jungleLowerIndex,int jungleUpperIndex, int height, int count) {

        this.width = width;
        this.height = height;
        this.count = count;
        this.jungleLowerIndex = jungleLowerIndex;
        this.jungleUpperIndex = jungleUpperIndex;
    }

    @Override
    public Iterator<Vector2d> iterator() {

        return new Iterator<>() {
            private final Random rand = new Random();
            private final List<Vector2d> indexes = new ArrayList<>();
            private int returned = 0;

            {
                for (int i = 0; i < height; i++) {
                    if (i<jungleLowerIndex || i > jungleUpperIndex) {
                        for (int j = 0; j < width; j++) {
                            indexes.add(new Vector2d(j, i));
                            IO.println(new Vector2d(j, i));
                        }
                    }
                }
            }

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

