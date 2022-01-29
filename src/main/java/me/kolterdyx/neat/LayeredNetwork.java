package me.kolterdyx.neat;

import me.kolterdyx.neat.utils.Configuration;
import me.kolterdyx.neat.utils.math.ActivationFunction;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class LayeredNetwork {
    private SimpleMatrix exposedLayer;
    private final ArrayList<SimpleMatrix> hiddenLayers;

    private final int inputs;
    private final int outputs;

    private final Random rng;
    private final Configuration config;

    public LayeredNetwork(int inputs, int outputs, Configuration config, Random rng){
        hiddenLayers = new ArrayList<>();
        this.rng = rng;
        this.inputs = inputs;
        this.outputs = outputs;
        this.config = config;
        SimpleMatrix initialMatrix = new SimpleMatrix(inputs, outputs);
        exposedLayer = randomizeMatrixOnes(initialMatrix);
        if (config.getBoolean("network.fill-with-default-value")){
            exposedLayer.fill(config.getDouble("network.default-weight-value"));
        }
    }

    public LayeredNetwork(int inputs, int outputs, Configuration config){
        this(inputs, outputs, config, new Random());
    }

    private SimpleMatrix randomizeMatrix(SimpleMatrix matrix){
        for (int i = 0; i < matrix.getNumElements(); i++) {
            matrix.set(i, rng.nextDouble()*config.getDouble("network.weight-range"));
        }
        return matrix.copy();
    }


    private SimpleMatrix randomizeMatrixOnes(SimpleMatrix matrix){
        for (int i = 0; i < matrix.getNumElements(); i++) {
            matrix.set(i, rng.nextBoolean() ? -1 : 1);
        }
        return matrix.copy();
    }

    public void addLayer(){
        if (hiddenLayers.size()==0){
            SimpleMatrix[] newLayers = splitMatrix(exposedLayer);
            SimpleMatrix inputLayer = newLayers[0].copy();
            SimpleMatrix hiddenLayer = newLayers[1].copy();
            exposedLayer = inputLayer;
            hiddenLayers.add(hiddenLayer);
        } else {
            SimpleMatrix[] newLayers = splitMatrix(hiddenLayers.get(hiddenLayers.size()-1));
            hiddenLayers.remove(hiddenLayers.size()-1);
            hiddenLayers.add(newLayers[0]);
            hiddenLayers.add(newLayers[1]);
        }
    }

    public ArrayList<SimpleMatrix> getLayers(){
        return hiddenLayers;
    }

    public void addNode(int index){
        if (hiddenLayers.size()==0) {
            addLayer();
            return;
        }

        double[][] array;
        double[][] newArray;

        // add connections after the new node
        array = matrixToArray(hiddenLayers.get(index));
        newArray = new double[array.length+1][array[0].length];

        for (int i=0; i< array.length;i++){
            newArray[i] = array[i];
        }
        newArray[newArray.length-1] = new double[newArray[0].length];
        for (int i = 0; i < newArray[0].length; i++) {
            newArray[newArray.length-1][i] = rng.nextDouble();
        }
        hiddenLayers.set(index, new SimpleMatrix(newArray));


        // add connections before the new node
        if (index==0){
            array = matrixToArray(exposedLayer);
        } else {
            array = matrixToArray(hiddenLayers.get(index-1));
        }
        newArray = new double[array.length][array[0].length+1];
        for (int i=0; i< array.length;i++){
            for (int j=0; j< array[0].length;j++){
                newArray[i][j] = array[i][j];
            }
            newArray[i][newArray[0].length-1] = rng.nextDouble();
        }

        if (index==0){
            exposedLayer = new SimpleMatrix(newArray);
        } else {
            hiddenLayers.set(index-1, new SimpleMatrix(newArray));
        }



    }

    public void removeNode(int layerIndex, int nodeIndex){
        if (hiddenLayers.size()==0) return;

        double[][] array;
        double[][] newArray;

        // remove connections after the new node
        array = matrixToArray(hiddenLayers.get(layerIndex));
        if (array.length == 1) return; // avoid removing the last node
        newArray = new double[array.length-1][array[0].length];

        for (int i=0; i< newArray.length;i++){
            newArray[i] = array[i];
        }
        hiddenLayers.set(layerIndex, new SimpleMatrix(newArray));


        // remove connections before the new node
        if (layerIndex==0){
            array = matrixToArray(exposedLayer);
        } else {
            array = matrixToArray(hiddenLayers.get(layerIndex-1));
        }
        newArray = new double[array.length][array[0].length-1];
        for (int i=0; i< array.length;i++){
            for (int j=0; j< newArray[0].length;j++) {
                newArray[i][j] = array[i][j];
            }
        }

        if (layerIndex==0){
            exposedLayer = new SimpleMatrix(newArray);
        } else {
            hiddenLayers.set(layerIndex-1, new SimpleMatrix(newArray));
        }
    }

    private double[][] matrixToArray(SimpleMatrix matrix) {
        double[][] array = new double[matrix.numRows()][matrix.numCols()];
        for (int r = 0; r < matrix.numRows(); r++) {
            for (int c = 0; c < matrix.numCols(); c++) {
                array[r][c] = matrix.get(r, c);
            }
        }
        return array;
    }

    public SimpleMatrix feed(SimpleMatrix X){

        ActivationFunction f = ActivationFunction.fromName(config.getString("network.default-activation"));
        ActivationFunction of = ActivationFunction.fromName(config.getString("network.output-activation"));

        SimpleMatrix result = f.calculateMatrix(X.mult(exposedLayer));

        if (hiddenLayers.size()>0){
            for (int i=0; i< hiddenLayers.size();i++){
                SimpleMatrix matrix = hiddenLayers.get(i);
                if (i< hiddenLayers.size()-1) result = f.calculateMatrix(result.mult(matrix));
                else result = of.calculateMatrix(result.mult(matrix));

            }
        }
        return result;
    }

    public void mutateWeight(){
        int layer = rng.nextInt(hiddenLayers.size()+1)-1;
        SimpleMatrix matrix;
        if (layer==-1){
            matrix = exposedLayer.copy();
        } else {
            matrix = hiddenLayers.get(layer).copy();
        }
        int node = rng.nextInt(matrix.getNumElements());
        double strength = config.getDouble("network.weight-range");
        matrix.set(node, matrix.get(node)+rng.nextDouble(strength*2f-strength));
        if (layer==-1){
            exposedLayer = matrix;
        } else {
            hiddenLayers.set(layer, matrix);
        }

    }

    private SimpleMatrix[] splitMatrix(SimpleMatrix matrix){
        SimpleMatrix[] result = new SimpleMatrix[2];

        double[][] array = new double[matrix.numRows()][matrix.numCols()];

        for (int row = 0; row < matrix.numRows(); row++) {
            for (int col = 0; col < matrix.numCols(); col++) {
                array[row][col] = matrix.get(row, col);
            }
        }

        result[0] = new SimpleMatrix(matrix.numRows(), 1);
        result[1] = new SimpleMatrix(1, matrix.numCols());

        for (int j = 0; j < matrix.numCols(); j++){
            double b = 0;
            for (int i = 0; i < matrix.numRows(); i++) {
                double a = Arrays.stream(array[i]).sum();
                b += array[i][j];
                result[0].set(i, a);
            }
            result[1].set(j, b);
        }

        return result;
    }

    @Override
    public String toString() {
        return ""+exposedLayer+" "+hiddenLayers;
    }

    public String geneDistribution(){
        String distribution = "";
        for (int i = 0; i < hiddenLayers.size(); i++) {
            distribution += hiddenLayers.get(i).numRows();
        }
        return distribution;
    }
}
