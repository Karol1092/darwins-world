package agh.ics.oop.util;

public record SimulationConfig(
        Map map,
        Energy energy,
        Animal animal,
        Genotype genotype,
        Fire fire
) {
    public record Map(
        int height,
        int width,
        int numberOfGrass,
        int numberOfGrassSpawn
    ) {}

    public record Energy(
        int grassProfit,
        int dailyLoss,
        int minimumToReproduce,
        int lossDueToReproduction
    ) {}

    public record Animal(
        int numberAtStart,
        int energyAtStart
    ) {}

    public record Genotype(
        int minimumMutations,
        int maximumMutations,
        int length
    ) {}

    public record Fire(
            double probability,
            int lasting,
            int damage
    ) {}
}
