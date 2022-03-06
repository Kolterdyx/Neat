package me.kolterdyx.neat.genome;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.utils.network.InnovationRegistry;

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
    @Expose
    private Node inputNodeInstance;
    @Expose
    private Node outputNodeInstance;

    public Connection(Node inputNodeInstance, Node outputNodeInstance, Random random, double weightLimit){
        GENE_TYPE = Gene.CONNECTION;
        this.inputNode = inputNodeInstance.getInnovation();
        this.outputNode = outputNodeInstance.getInnovation();
        this.innovation = InnovationRegistry.getConnection(inputNode, outputNode);

        this.inputNodeInstance = inputNodeInstance;
        this.outputNodeInstance = outputNodeInstance;

        this.weight = Double.parseDouble(String.format("%.5f", (random.nextDouble()-.5f)*2*weightLimit).replace(',', '.'));
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

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Node getInputNodeInstance() {
        return inputNodeInstance;
    }

    public Node getOutputNodeInstance() {
        return outputNodeInstance;
    }
}
