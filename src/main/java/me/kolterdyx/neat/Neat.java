package me.kolterdyx.neat;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.utils.data.Configuration;
import me.kolterdyx.neat.utils.neural.Serializer;
import org.ejml.simple.SimpleMatrix;
import org.graphstream.graph.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Neat {
    private transient Random random;

    @Expose
    private Configuration config;

    @Expose
    private Network network;

    public Neat(Configuration config){
        this.config = config;
        random = new Random();

        if (config.getBoolean("network.random.use-seed")){
            random.setSeed(config.getInt("network.random.seed"));
        }

        int inputs = config.getInt("network.inputs");
        int outputs = config.getInt("network.outputs");

        network = new Network(inputs, outputs, config);
    }

    public static Neat deserialize(String data) {
        return Serializer.deserialize(data);
    }

    public SimpleMatrix feed(SimpleMatrix X){
        return network.feed(X);
    }


    public boolean tryMutation(){
        if (random.nextDouble() < config.getDouble("network.mutation.chance")){
            final double[] totalWeight = {0f};

            double[] weights = new double[]{
                    config.getDouble("network.mutation.weights.weight"),
                    config.getDouble("network.mutation.weights.bias"),
                    config.getDouble("network.mutation.weights.add-node"),
                    config.getDouble("network.mutation.weights.remove-node"),
                    config.getDouble("network.mutation.weights.add-connection"),
                    config.getDouble("network.mutation.weights.remove-connection"),
            };

            Arrays.stream(weights).forEach(value -> totalWeight[0]+=value);

            Runnable[] options = new Runnable[]{
                    network::mutateWeight,
                    network::mutateBias,
                    network::addRandomNode,
                    network::removeRandomNode,
                    network::addRandomConnection,
                    network::removeRandomConnection,
            };

            int choice = 0;
            for (double r = random.nextDouble() * totalWeight[0]; choice < options.length - 1; ++choice) {
                r -= weights[choice];
                if (r <= 0.0) break;
            }

            options[choice].run();
            return true;
        }
        return false;
    }

    public void plotGraph() {
        network.plotGraph();
    }

    @Override
    public String toString() {
        return ""+network;
    }

    public Neat copy() {
        String data = Serializer.serialize(this);
        try {
            FileWriter file = new FileWriter("network.json");
            file.write(data);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Serializer.deserialize(data);
    }

    public void postProcess() {
        random = new Random();
        if (config.getBoolean("network.random.use-seed")) {
            random.setSeed(config.getInt("network.random.seed"));
        }

        network.setConfig(config);
        network.setRandom(random);
        network.createGraph();
    }

    public Graph getGraph(){
        return network.getGraph();
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof Neat net){
            return toString().equals(net.toString());
        }
        return false;
    }

    public String serialize() {
        return Serializer.serialize(this);
    }

    public void exportToFile(String filename) throws IOException {
        FileWriter file = new FileWriter(filename);
        file.write(this.serialize());
        file.close();
    }

    public static Neat importFromFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        String data = "";
        while (scanner.hasNextLine()){
            data += scanner.nextLine().replace("  ", "");
        }
        return deserialize(data);
    }

}
