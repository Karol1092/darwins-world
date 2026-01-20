package agh.ics.oop.model.world.map;


import agh.ics.oop.model.world.element.Animal;
import agh.ics.oop.model.world.element.Grass;
import agh.ics.oop.model.world.element.WorldDirections;
import agh.ics.oop.model.world.element.WorldElement;
import agh.ics.oop.presenter.Observer;
import agh.ics.oop.util.JungleGrassPositionsGenerator;
import agh.ics.oop.util.SimulationConfig;
import agh.ics.oop.util.SteppeGrassPositionsGenerator;
import agh.ics.oop.util.Vector2d;
import com.sun.javafx.geom.Vec2d;

import java.util.*;

public class WorldMap{
    private final HashMap<Vector2d,List<Animal>> animals = new HashMap<>();
    private final HashMap<Vector2d, Grass> grasses = new HashMap<>();
    private final HashMap<Vector2d,List<WorldElement>> elementsOnFire = new HashMap<>();
    private final HashSet<Vector2d> fires = new HashSet<>();
    private final Vector2d lowerLeft = new Vector2d(0,0);
    private final Vector2d upperRight;
    protected final List<Observer> observers = new ArrayList<>();
    private final JungleGrassPositionsGenerator jungleGenerator;
    private final SteppeGrassPositionsGenerator steppeGenerator;
    private final int jungleMinHeight;
    private final int jungleMaxHeight;
    private final Vector2d[] neighbours = {new Vector2d(0,1),new Vector2d(0,0),new Vector2d(1,1), new Vector2d(1,0),
            new Vector2d(0,-1),new Vector2d(-1,0),new Vector2d(-1,1),new Vector2d(-1,-1)};
    private final Random random = new Random();
    private final int[] jungleToSteppeRatio = {0,0,0,0,1};
    private final static WorldDirections[] worldDirectionsPool =     {
            WorldDirections.NORTH,
            WorldDirections.NORTH_EAST,
            WorldDirections.EAST,
            WorldDirections.SOUTH_EAST,
            WorldDirections.SOUTH,
            WorldDirections.SOUTH_WEST,
            WorldDirections.WEST,
            WorldDirections.NORTH_WEST};
    private final  SimulationConfig config;
    private final HashMap<Vector2d,Integer> vectorCooldown = new HashMap<>();

    public WorldMap(SimulationConfig config) {
        this.upperRight = new Vector2d((config.map().width() - 1), (config.map().height() - 1));
        this.config = config;
        int height = config.map().height();
        int width = config.map().width();
        int jungleHeight = Math.max(1, (int)Math.round(height * 0.2));
        this.jungleMinHeight = (height - jungleHeight) / 2;
        this. jungleMaxHeight = jungleMinHeight + jungleHeight - 1;
        int jungleSize = (jungleMaxHeight-jungleMinHeight+1)*width;
        int mapSize = width*height;


        this.jungleGenerator = new JungleGrassPositionsGenerator(width,
                jungleMinHeight, jungleMaxHeight, jungleSize);
        this.steppeGenerator = new SteppeGrassPositionsGenerator(width,
                jungleMinHeight, jungleMaxHeight, height, mapSize - jungleSize);


    }

    public boolean isOccupied(Vector2d position){
        List<Animal> animalsAtPosition = animals.get(position);
        return (animalsAtPosition != null && !animalsAtPosition.isEmpty())
                || grasses.containsKey(position);
    }

    private boolean grassLocation(Vector2d position){
        return position.getY() >= jungleMinHeight && position.getY() <= jungleMaxHeight;
    }

    public Grass getGrassAtPosition(Vector2d position) {
        return grasses.getOrDefault(position, null);
    }

    public List<Grass> getAllGrasses() {
        return new ArrayList<>(grasses.values());
    }

    public Set<Vector2d> getAllGrassesPositions() {
        return new HashSet<>(grasses.keySet());
    }

    public List<Vector2d> getAllElementsPositions() {
        Set<Vector2d> allPositions = new HashSet<>(animals.keySet());
        allPositions.addAll(grasses.keySet());

        return new ArrayList<>(allPositions);
    }

    public void grassPlacement(int count) {
        int place = 0;
        Iterator<Vector2d> jungleIterator = jungleGenerator.iterator();
        Iterator<Vector2d> steppeIterator = steppeGenerator.iterator();
        for (int i = 0; i < count; i++) {
            place = random.nextInt(jungleToSteppeRatio.length);
            if (jungleToSteppeRatio[place]==0){
                if (jungleIterator.hasNext()){
                    place(new Grass(jungleIterator.next()));
                }else if (steppeIterator.hasNext()) {
                    place(new Grass(steppeIterator.next()));
                }
            }else if (steppeIterator.hasNext()){
                place(new Grass(steppeIterator.next()));
            }
        }
    }

    public void removeGrass(Grass grass) {
        if (grassLocation(grass.getPosition())) {
            jungleGenerator.addIndex(grass.getPosition());
        }else{
            steppeGenerator.addIndex(grass.getPosition());
        }
        grasses.remove(grass.getPosition(), grass);
    }

    public void animalsGrassEating() {

        List<Animal> sortedAnimals = new ArrayList<>();

        for (List<Animal> animalsAtPosition : animals.values()) {
            sortedAnimals.addAll(animalsAtPosition);
        }

        sortedAnimals.sort(Comparator.comparingInt(Animal::getLifeEnergy)
                .thenComparingInt(Animal::getAge)
                .thenComparingInt(Animal::getNumberOfChildren)
                .thenComparing(Animal::getUniqueId));

        for (Animal animal : sortedAnimals) {
            Grass grass = getGrassAtPosition(animal.getPosition());
            if (grass != null && !grass.getIsBurning()) {
                if (random.nextInt(100)>config.fire().probability()*100) {
                    removeGrass(grass);
                    animal.setLifeEnergy(animal.getLifeEnergy() + config.energy().grassProfit());
                }else{
                    fires.add(grass.getPosition());
                    elementsOnFire.computeIfAbsent(animal.getPosition(), p -> new ArrayList<>()).add(animal);
                    elementsOnFire.computeIfAbsent(grass.getPosition(), p -> new ArrayList<>()).add(grass);
                    animal.setBurning(config.fire().lasting());
                    animal.setIsBurning(true);
                    grass.setIsBurning(true);
                    grass.setBurning(config.fire().lasting());
                }
            }
        }
    }

    public List<WorldElement> getAllElements(){
        List<WorldElement> elements = new ArrayList<>();
        for (int i = 0; i<upperRight.getX()+1; i++){
            for (int j = 0; j<upperRight.getY()+1; j++) {
                Vector2d position = new Vector2d(i, j);
                if (isOccupied(position)) {
                    if (grasses.containsKey(position)) elements.add(grasses.get(position));
                    List<Animal> animalsAtPosition = animals.get(position);
                    if (animalsAtPosition != null) elements.addAll(animalsAtPosition);
                }
            }
        }
        return elements;
    }

    public void place(WorldElement element) {
        if (element instanceof Grass) {
            grasses.put(element.getPosition(), (Grass) element);
        }
        else if (element instanceof Animal) {
            animals.computeIfAbsent(element.getPosition(), k  -> new ArrayList<>()).add((Animal) element);
        }
    }

    public List<Animal> animalsReproduction(){
        List<Animal> newborns = new ArrayList<>();

        for (Map.Entry<Vector2d,List<Animal>> entry : animals.entrySet()){
            Vector2d position = entry.getKey();
            List<Animal> animalsAtPosition = entry.getValue();
            if(animalsAtPosition.size() >= 2){
                List<Animal> ready = new ArrayList<>(animalsAtPosition.stream()
                        .filter(a-> !a.getIsBurning())
                        .filter(a -> a.getLifeEnergy() >= config.energy().minimumToReproduce())
                        .toList());
                if (ready.size() >= 2) {
                    newborns.addAll(reproduceAt(position, ready));
                };
            }
        }
        for (Animal baby : newborns) {
            place(baby);
        }
        return newborns;
    }

    private List<Animal> reproduceAt(Vector2d position, List<Animal> ready){
        List<Animal> children = new ArrayList<>();
        while(ready.size() >= 2) {
            ready.sort(Comparator.comparingInt(Animal::getLifeEnergy)
                    .thenComparingInt(Animal::getAge)
                    .thenComparingInt(Animal::getNumberOfChildren)
                    .thenComparing(Animal::getUniqueId));

            Animal mom = ready.getLast();
            Animal dad = ready.get(ready.size() - 2);

            if (mom.getLifeEnergy() >= config.energy().minimumToReproduce() &&
            dad.getLifeEnergy() >= config.energy().minimumToReproduce()) {

                int totalEnergy = dad.getLifeEnergy() +  mom.getLifeEnergy();
                int genotypeLength = config.genotype().length();

                int momGenesRatio = (int) Math.round(((double) mom.getLifeEnergy() / totalEnergy) * genotypeLength);
                int dadGenesRatio = genotypeLength - momGenesRatio;

                boolean side = random.nextBoolean();
                List<Integer> result = new ArrayList<>();

                if (side) {
                    result.addAll(new ArrayList<>(mom.getGene().subList(0, momGenesRatio)));
                    result.addAll(new ArrayList<>(dad.getGene().subList(momGenesRatio, genotypeLength)));
                } else {
                    result.addAll(new ArrayList<>(dad.getGene().subList(0, dadGenesRatio)));
                    result.addAll(new ArrayList<>(mom.getGene().subList(dadGenesRatio, genotypeLength)));
                }

                mom.setLifeEnergy(mom.getLifeEnergy() - config.energy().lossDueToReproduction());
                dad.setLifeEnergy(dad.getLifeEnergy() - config.energy().lossDueToReproduction());

                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                    indices.add(i);
                }

                Collections.shuffle(indices);
                int mutationsCount = random.nextInt(
                        config.genotype().minimumMutations(),
                        config.genotype().maximumMutations() + 1
                );

                int actualMutations = Math.min(mutationsCount, result.size());

                for (int i = 0; i < actualMutations; i++) {
                    int idx = indices.get(i);
                    result.set(idx, random.nextInt(8));
                }

                Animal child = new Animal(position, worldDirectionsPool[random.nextInt(8)], result, 2 * config.energy().lossDueToReproduction());
                children.add(child);

                mom.setNumberOfChildren(mom.getNumberOfChildren() + 1);
                dad.setNumberOfChildren(dad.getNumberOfChildren() + 1);

                ready.remove(mom);
                ready.remove(dad);

            } else {
                break;
            }
        }
        return children;
    }

    public void removeAnimal(Animal animal) {
        Vector2d position = animal.getPosition();
        List<Animal> animalsAtPosition = animals.get(position);

        if (animalsAtPosition != null) {
            animalsAtPosition.remove(animal);

            if (animalsAtPosition.isEmpty()) {
                animals.remove(position);
            }
        }
    }

    private int verticalPositionCheck(Animal animal, WorldDirections facingDirection) throws Exception {
        int y = animal.getPosition().getY();
        if (y > upperRight.getY() || y < lowerLeft.getY()) {
            animal.setFacingDirection(WorldDirections.bounce(facingDirection));
            animal.move(0);
            if (animal.getFacingDirection() == WorldDirections.NORTH
                    || animal.getFacingDirection() == WorldDirections.SOUTH) {
                animal.move(0);
            }
        }
        return animal.getPosition().getY();
    }

    public void move(Animal animal,int rotation) throws Exception{
        removeAnimal(animal);
        animal.move(rotation);
        WorldDirections facingDirection = animal.getFacingDirection();
        int y = verticalPositionCheck(animal,facingDirection);
        int range = upperRight.getX()-lowerLeft.getX()+1;
        int x = ((animal.getPosition().getX() - lowerLeft.getX()) % range + range) % range + lowerLeft.getX();

        animal.setPosition(new Vector2d(x,y));
        animals.computeIfAbsent(animal.getPosition(), k -> new ArrayList<>()).add(animal);

        if (fires.contains(animal.getPosition()) && !animal.getIsBurning()){
            elementsOnFire.computeIfAbsent(animal.getPosition(), k -> new ArrayList<>()).add(animal);
            animal.setBurning(config.fire().lasting());
            animal.setIsBurning(true);
        }
    }

    public void fireDamage(){
        List<Vector2d> noFirePositions = new ArrayList<>();
        for (Map.Entry<Vector2d,List<WorldElement>> entry: elementsOnFire.entrySet()){
            Vector2d position = entry.getKey();
            List<WorldElement> elementsOnFireAtPosition = entry.getValue();
            List<WorldElement> elementsNoLongerOnFire = new ArrayList<>();
                for (WorldElement worldElement : elementsOnFireAtPosition){
                    worldElement.setBurning(worldElement.getBurning()-1);
                    if (worldElement.getBurning() <= 0) {
                        if (worldElement instanceof Grass){
                            fires.remove(position);
                            worldElement.setIsBurning(false);
                            grasses.remove(position);
                            vectorCooldown.put(worldElement.getPosition(),config.fire().lasting()-1);
                        }
                        elementsNoLongerOnFire.add(worldElement);
                        worldElement.setIsBurning(false);
                    }else {
                        if (worldElement instanceof Animal) {
                            ((Animal) worldElement).setLifeEnergy(Math.max(0, ((Animal) worldElement).getLifeEnergy() - config.fire().damage()));
                        }
                    }
                }

                elementsOnFireAtPosition.removeAll(elementsNoLongerOnFire);

                if (elementsOnFireAtPosition.isEmpty()){
                    noFirePositions.add(position);
                }
        }
        for (Vector2d position : noFirePositions){
            elementsOnFire.remove(position);
            fires.remove(position);
        }
    }

    public void fireSpreading() {
        Set<Vector2d> newFire = new HashSet<>();
        for (Vector2d position: fires){
            for (Vector2d v : neighbours){
                Vector2d pos =  position.add(v);
                if (grasses.containsKey(pos) && !grasses.get(pos).getIsBurning()){
                    newFire.add(pos);
                    Grass grass = grasses.get(pos);
                    grass.setIsBurning(true);
                    grass.setBurning(config.fire().lasting());
                    elementsOnFire.computeIfAbsent(pos, k -> new ArrayList<>()).add(grass);
                }
            }
        }
        fires.addAll(newFire);
    }

    public void reloadGenerators(){
        List<Vector2d> toRemove = new ArrayList<>();
        for (Map.Entry<Vector2d,Integer> entry: vectorCooldown.entrySet()){
            Vector2d position = entry.getKey();
            int cooldown = entry.getValue()-1;
            if (cooldown <= 0){
                toRemove.add(position);
                if(grassLocation(position)) jungleGenerator.addIndex(position);
                else steppeGenerator.addIndex(position);
            }else vectorCooldown.put(position,cooldown);
        }
        for (Vector2d position : toRemove){
            vectorCooldown.remove(position);
        }
    }

    public Vector2d getLowerLeft() {
        return lowerLeft;
    }

    public Vector2d getUpperRight() {
        return  upperRight;
    }

    public void addObserver(Observer observer){
        observers.add(observer);
    }

    public void removeObserver(Observer observer){
        observers.remove(observer);
    }

    public void mapChanged(String message) {
        for (Observer observer : observers) {
            observer.mapChanged(this, message);
        }
    }
}

