package me.kolterdyx.neat;

import me.kolterdyx.neat.utils.Configuration;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Random;

public class Neat {
    private int fitness;

    private Configuration config;

    private LayeredNetwork lnetwork;
    private NeatNetwork nnetwork;
    private Random random;
    private boolean layered;

    public Neat(Configuration config){
        this.config = config;
        random = new Random();
        layered = config.getBoolean("network.layered");

        if (config.getBoolean("network.random.use-seed")){
            random.setSeed(config.getInt("network.random.seed"));
        }

        int inputs = config.getInt("network.inputs");
        int outputs = config.getInt("network.outputs");

        if (layered){
            lnetwork = new LayeredNetwork(inputs, outputs, config);
        } else {
            nnetwork = new NeatNetwork(inputs, outputs, config);
            nnetwork.addRandomNode();
        }
    }

    public SimpleMatrix feed(SimpleMatrix X){
        if (layered) return lnetwork.feed(X);
        else return nnetwork.feed(X);
    }

    public ArrayList<SimpleMatrix> getLayers(){
        if (layered) return lnetwork.getLayers();
        else return null;
    }

    public void tryMutation(){
        if (random.nextDouble() < config.getDouble("network.mutation.chance")){
            double lTotalWeight = 0f;
            double nTotalWeight = 0f;

            double[] lweights = new double[]{
                    config.getDouble("network.mutation.weights.weight"),
                    config.getDouble("network.mutation.weights.remove-node"),
                    config.getDouble("network.mutation.weights.add-node"),
                    config.getDouble("network.mutation.weights.add-layer")
            };
            double[] nweights = new double[]{
                    config.getDouble("network.mutation.weights.weight"),
                    config.getDouble("network.mutation.weights.add-node"),
                    config.getDouble("network.mutation.weights.remove-node"),
                    config.getDouble("network.mutation.weights.add-connection"),
                    config.getDouble("network.mutation.weights.remove-connection")
            };

            lTotalWeight += lweights[0];
            lTotalWeight += lweights[1];
            lTotalWeight += lweights[2];
            lTotalWeight += lweights[3];

            nTotalWeight += nweights[0];

            int[] loptions = new int[]{0, 1, 2, 3};
            int[] noptions = new int[]{0, 1, 2, 4, 5};

            int idx = 0;
            if (layered) {
                for (double r = random.nextDouble() * lTotalWeight; idx < loptions.length - 1; ++idx) {
                    r -= lweights[idx];
                    if (r <= 0.0) break;
                }
            } else {
                for (double r = random.nextDouble() * nTotalWeight; idx < noptions.length - 1; ++idx) {
                    r -= nweights[idx];
                    if (r <= 0.0) break;
                }
            }


            switch (idx){
                case 0 -> {
                    if (layered) lnetwork.mutateWeight();
                    else nnetwork.mutateWeight();
                }
                case 1 -> {
                    if (layered) {
                        if (lnetwork.getLayers().size() < 1) return;
                        int layer = random.nextInt(lnetwork.getLayers().size());
//                        if (lnetwork.getLayers().get(layer).numRows() <= 1) return;
//                        int node = random.nextInt(random.nextInt(lnetwork.getLayers().get(layer).numRows()));
                        lnetwork.removeNode(layer, 0);
                    }
                    else nnetwork.removeRandomNode();
                }
                case 2 -> {
                    if (layered) {

                        if (lnetwork.getLayers().size() < 1) return;
                        int n = random.nextInt(lnetwork.getLayers().size());
                        lnetwork.addNode(n);

                    }
                    else nnetwork.addRandomNode();
                }
                case 3 -> {
                    lnetwork.addLayer();
                }
                case 4 -> {
                    nnetwork.addRandomConnection();
                }
                case 5 -> {
                    nnetwork.removeRandomConnection();
                }
            }

        }
    }
}
