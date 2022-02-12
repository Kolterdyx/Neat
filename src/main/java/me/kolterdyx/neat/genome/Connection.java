package me.kolterdyx.neat.genome;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.utils.neural.InnovationRegistry;

import java.util.Random;

public class Connection extends Gene {
    @Expose
    private double weight;
    @Expose
    private int inputNode;
    @Expose
    private int outputNode;
    @Expose
    private boolean enabled;

    public Connection(int inputNode, int outputNode, Random random, double weightLimit){
        GENE_TYPE = Gene.CONNECTION;
        this.innovation = InnovationRegistry.getConnection(inputNode, outputNode);
        this.inputNode = inputNode;
        this.outputNode = outputNode;
        this.weight = Double.parseDouble(String.format("%.5f", (random.nextDouble()-.5f)*2).replace(',', '.'))*weightLimit;
        enable();
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean enabled() {
        return enabled;
    }

    public int getOutputNode() {
        return outputNode;
    }

    public int getInputNode() {
        return inputNode;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "{"+innovation+", "+getInputNode()+" -> "+getOutputNode()+", "+enabled+", "+weight+"}";
    }
}
