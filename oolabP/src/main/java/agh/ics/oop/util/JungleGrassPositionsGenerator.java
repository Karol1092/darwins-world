package agh.ics.oop.util;

import agh.ics.oop.util.Vector2d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class JungleGrassPositionsGenerator implements Iterable<Vector2d> {

    private final int width;
    private final int minHeight;
    private final int maxHeight;
    private final int count;


    public JungleGrassPositionsGenerator(int width,int minHeight,int maxHeight,int count) {

        this.width = width;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.count = count;
    }

    @Override
    public Iterator<Vector2d> iterator() {

        return new Iterator<>() {
            private final Random rand = new Random();
            private final List<Vector2d> indexes = new ArrayList<>();
            private int returned = 0;

            {
                for (int i = 0; i < width; i++) {
                    for (int j = minHeight; j < (maxHeight+1); j++) {
                        indexes.add(new Vector2d(i, j));
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

//            public void addIndex(Vector2d index) {
//                indexes.add(index);
//            }
        };
    }
}

