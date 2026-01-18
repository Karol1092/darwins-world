package agh.ics.oop.model.world.element;

import agh.ics.oop.util.Vector2d;

    public class Grass implements WorldElement{
        private final Vector2d position;
        private boolean isBurning;
        private int burning;
        public Grass(Vector2d position){
            this.position = position;
        }
        @Override
        public boolean getIsBurning() {
            return isBurning;
        }
        @Override
        public void setIsBurning(boolean isBurning) {
            this.isBurning = isBurning;
        }
        @Override
        public int getBurning(){
            return this.burning;
        }
        @Override
        public void  setBurning(int burning){
            this.burning = burning;
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
