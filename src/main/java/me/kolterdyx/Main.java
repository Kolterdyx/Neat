package me.kolterdyx;

import me.kolterdyx.neat.Network;
import me.kolterdyx.neat.utils.PopulationThread;
import me.kolterdyx.utils.Configuration;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;

import static me.kolterdyx.neat.utils.Signal.*;

public class Main {

    static Network net;
    static BufferedImage image;
    static WritableRaster raster;

    public static void main(String[] args) throws InterruptedException {
        Configuration config = new Configuration("config.yml");
        PopulationThread thread = new PopulationThread(config, 3);

        thread.start();

//        System.out.println(thread);

        thread.sendData(MUTATE_ALL, 10);

        int t=0;
        HashMap<Long, double[]> inputData = new HashMap<>();
        do {
            for (long id : thread.getIdList()) {
                inputData.put(id, new double[]{Math.random()});
            }

            thread.sendData(INPUT_DATA, inputData);

            thread.sendData(MUTATE_ALL, 1);

            t++;
        } while (t != 1000);


        thread.sendData(CLOSE, null);
        thread.join();


    }
}
