package agh.ics.oop.util;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String DIR = "configs";

    public void save(String fileName, SimulationConfig config) {
        File directory = new File(DIR);
        if (!directory.exists()) directory.mkdirs();

        File file = new File(directory, fileName.endsWith(".properties") ? fileName : fileName + ".properties");
        Properties props = new Properties();

        props.setProperty("height", String.valueOf(config.map().height()));
        props.setProperty("width", String.valueOf(config.map().width()));
        props.setProperty("numberOfGrass", String.valueOf(config.map().numberOfGrass()));
        props.setProperty("numberOfGrassSpawn", String.valueOf(config.map().numberOfGrassSpawn()));

        props.setProperty("grassProfit", String.valueOf(config.energy().grassProfit()));
        props.setProperty("dailyLoss", String.valueOf(config.energy().dailyLoss()));
        props.setProperty("minimumToReproduce", String.valueOf(config.energy().minimumToReproduce()));
        props.setProperty("lossDueToReproduction", String.valueOf(config.energy().lossDueToReproduction()));

        props.setProperty("numberAtStart", String.valueOf(config.animal().numberAtStart()));
        props.setProperty("energyAtStart", String.valueOf(config.animal().energyAtStart()));

        props.setProperty("minimumMutations", String.valueOf(config.genotype().minimumMutations()));
        props.setProperty("maximumMutations", String.valueOf(config.genotype().maximumMutations()));
        props.setProperty("length", String.valueOf(config.genotype().length()));

        props.setProperty("probability", String.valueOf(config.fire().probability()));
        props.setProperty("lasting", String.valueOf(config.fire().lasting()));
        props.setProperty("damage", String.valueOf(config.fire().damage()));

        try (OutputStream out = new FileOutputStream(file)) {
            props.store(out, "World config");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Properties load(File file) throws IOException {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);
        }
        return props;
    }
}
