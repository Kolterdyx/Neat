package me.kolterdyx.neat;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.utils.Configuration;
import me.kolterdyx.neat.utils.neural.Serializer;
import org.ejml.simple.SimpleMatrix;
import org.graphstream.graph.Graph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

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

    public SimpleMatrix feed(SimpleMatrix X){
        return network.feed(X);
    }


    public void tryMutation(){
        if (random.nextDouble() < config.getDouble("network.mutation.chance")){
            double totalWeight = 0f;

            double[] weights = new double[]{
                    config.getDouble("network.mutation.weights.weight"),
                    config.getDouble("network.mutation.weights.add-node"),
                    config.getDouble("network.mutation.weights.remove-node"),
                    config.getDouble("network.mutation.weights.add-connection"),
                    config.getDouble("network.mutation.weights.remove-connection")
            };

            totalWeight += weights[0];
            totalWeight += weights[1];
            totalWeight += weights[2];
            totalWeight += weights[3];
            totalWeight += weights[4];

            Runnable[] options = new Runnable[]{
                    network::mutateWeight,
                    network::addRandomNode,
                    network::removeRandomNode,
                    network::addRandomConnection,
                    network::removeRandomConnection
            };

            int choice = 0;
            for (double r = random.nextDouble() * totalWeight; choice < options.length - 1; ++choice) {
                r -= weights[choice];
                if (r <= 0.0) break;
            }

            options[choice].run();
        }
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

        network.createGraph();
        network.setConfig(config);
        network.setRandom(random);
    }

    public Graph getGraph(){
        return network.getGraph();
    }
}
