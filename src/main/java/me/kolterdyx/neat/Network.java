package me.kolterdyx.neat;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.genome.Genome;
import me.kolterdyx.utils.Configuration;
import me.kolterdyx.neat.utils.data.Serializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Network {
    private transient Random random;

    @Expose
    private Configuration config;

    @Expose
    private Genome genome;

    public Network(Configuration config){
        this.config = config;
        random = new Random();

        if (config.getBoolean("network.random.use-seed")){
            random.setSeed(config.getInt("network.random.seed"));
        }

        genome = new Genome(config);
    }

    /**
     * Creates a network from a serialized network string.
     * @param data Serialized network.
     * @return Network deserialized from the string.
     */
    public static Network deserialize(String data) {
        return Serializer._deserialize(data);
    }

    /**
     * Calculate network output given an input value array X.
     * @param X Input values. The array must have the same length as the input node layer.
     * @return Array of doubles containing output values. Same length as the output node layer.
     */
    public double[] feed(double[] X){
        return genome._feed(X);
    }


    /**
     * Attempts to mutate the network.
     * @return boolean whether the network was successfully mutated or not.
     */
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
                    genome::_mutateWeight,
                    genome::_mutateBias,
                    genome::_addRandomNode,
                    genome::_removeRandomNode,
                    genome::_addRandomConnection,
                    genome::_removeRandomConnection,
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

    @Override
    public String toString() {
        return ""+ genome;
    }

    /**
     * Copies a network.
     * @return an identical copy to this network.
     */
    public Network copy() {
        String data = Serializer._serialize(this);
        return Serializer._deserialize(data);
    }

    /**
     * Method called by the serializer. Please avoid calling this method.
     */
    public void _postProcess() {
        random = new Random();
        if (config.getBoolean("network.random.use-seed")) {
            random.setSeed(config.getInt("network.random.seed"));
        }

        genome._setConfig(config);
        genome._setRandom(random);
    }


    @Override
    public boolean equals(Object o){
        if (o instanceof Network net){
            return toString().equals(net.toString());
        }
        return false;
    }

    /**
     * Serialize the network.
     * @return String containing the serialized network. Can be written directly to a json file.
     */
    public String serialize() {
        return Serializer._serialize(this);
    }

    /**
     * Serializes and writes the network to a json file.
     * @param filename Output json file that will contain the network.
     * @throws IOException
     */
    public void exportToFile(String filename) throws IOException {
        FileWriter file = new FileWriter(filename);
        file.write(this.serialize());
        file.close();
    }

    /**
     * Reads a json file containing a network and deserializes it.
     * @param filename Json file containing a network.
     * @return Network produced from the file
     * @throws FileNotFoundException
     */
    public static Network importFromFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        String data = "";
        while (scanner.hasNextLine()){
            data += scanner.nextLine().replace("  ", "");
        }
        return deserialize(data);
    }

    public Genome _getGenome() {
        return genome;
    }

    public void _setGenome(Genome genome){
        this.genome = genome;
        genome._setConfig(this.config);
        genome._setRandom(this.random);
    }
}
