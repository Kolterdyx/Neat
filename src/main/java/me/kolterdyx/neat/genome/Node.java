package me.kolterdyx.neat.genome;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.utils.math.ActivationFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class Node extends Gene {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int HIDDEN = 2;
    public static final String[] NODE_TYPES = new String[]{"INPUT", "OUTPUT", "HIDDEN"};

    @Expose
    private int NODE_TYPE;

    @Expose
    private ArrayList<Double> inputValues = new ArrayList<>();

    @Expose
    private ArrayList<Integer> incomingConnections = new ArrayList<>();

    @Expose
    private double outputValue;
    @Expose
    private boolean processed = false;

    @Expose
    private ActivationFunction activationFunction;

    @Expose
    private int numberOfInputs=0;

    @Expose
    private double bias;

    public Node(int type, int innovation, ActivationFunction activationFunction, Random random, double biasRange){
        GENE_TYPE = Gene.NODE;
        NODE_TYPE = type;
        this.innovation = innovation;
        this.activationFunction = activationFunction;
        this.bias = Double.parseDouble(String.format("%.5f", (random.nextDouble()-.5f)*2).replace(',', '.'))*biasRange;
    }

    public void addNewInput(){
        numberOfInputs++;
    }

    public void addIncomingConnection(int con){
        addNewInput();
        if (!incomingConnections.contains(con)) incomingConnections.add(con);
    }

    public ArrayList<Integer> getIncomingConnections(){
        return incomingConnections;
    }

    public void removeInput(){
        numberOfInputs--;
    }

    public void addInputValue(double value){
        inputValues.add(value);
    }

    public boolean allInputsCalculated(){
        return inputValues.size() == numberOfInputs;
    }

    public void calculate(){
        double value = 0;
        for(double n : inputValues){
            value += n;
        }
        outputValue = activationFunction.calculate(value+bias);
//        System.out.println(this+" "+inputValues+" "+value+" "+incomingConnections);
    }


    public int getNodeType() {
        return NODE_TYPE;
    }

    public void reset() {
        processed = false;
        inputValues.clear();
    }

    public void setNumberOfInputs(int n){
        numberOfInputs = n;
    }

    public double getOutput() {
        return outputValue;
    }

    public boolean hasBeenProcessed() {
        return processed;
    }

    @Override
    public String toString() {
        return "{"+innovation+", "+NODE_TYPES[NODE_TYPE]+", "+numberOfInputs+", "+outputValue+"}";
    }

    public void setOutput(double value) {
        this.outputValue = value;
        processed = true;
    }

    public ArrayList<Double> getInputValues() {
        return inputValues;
    }
}
