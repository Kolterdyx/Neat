package me.kolterdyx.neat.genome;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class Node extends Gene {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int HIDDEN = 2;

    @Expose
    private final int NODE_TYPE;

    private ArrayList<Double> inputValues = new ArrayList<>();
    private double outputValue;
    private boolean processed = false;

    public Node(int type, int innovation){
        GENE_TYPE = Gene.NODE;
        NODE_TYPE = type;
        this.innovation = innovation;
    }

    public void calculate(){
        int value = 0;
        for(double n : inputValues){
            value += n;
        }
        outputValue = activationFunction(value);
    }

    private double activationFunction(double value) {
        return (1/(1+Math.exp(-value)));
    }

    public int getNodeType() {
        return NODE_TYPE;
    }
}
