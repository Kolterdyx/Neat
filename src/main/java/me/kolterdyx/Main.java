package me.kolterdyx;

import me.kolterdyx.neat.Neat;
import me.kolterdyx.neat.utils.PopulationThread;
import me.kolterdyx.neat.utils.data.Configuration;
import org.apache.commons.math3.util.Pair;
import org.bytedeco.opencv.opencv_dnn.ConcatLayer;
import org.ejml.simple.SimpleMatrix;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.kolterdyx.neat.utils.Signal.*;
import static org.bytedeco.opencv.helper.opencv_imgcodecs.cvLoadImage;

public class Main {

    static Neat net;
    static BufferedImage image;
    static WritableRaster raster;

    public static void main(String[] args) throws InterruptedException {
        Configuration config = new Configuration("config.yml");
        PopulationThread thread = new PopulationThread(config, 3);

        thread.start();


        System.out.println(thread);

        thread.sendData(MUTATE_ALL, 10);

        int t=0;
        HashMap<Long, SimpleMatrix> inputData = new HashMap<>();
        do {
            for (long id : thread.getIdList()) {
                inputData.put(id, new SimpleMatrix(new double[][]{new double[]{Math.random()}}));
            }

            thread.sendData(INPUT_DATA, inputData);

            thread.sendData(MUTATE_ALL, 1);

            t++;
        } while (t != 1000);


        thread.sendData(CLOSE, null);
        thread.join();


    }
}
