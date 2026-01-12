package agh.ics.oop.model.world.element;

import agh.ics.oop.util.Vector2d;

    public class Grass implements WorldElement{
        private final Vector2d position;
        public Grass(Vector2d position){
            this.position = position;
        }

        @Override
        public Vector2d getPosition() {
            return position;
        }
        @Override
        public String toString(){
            return "*";
        }
    }
