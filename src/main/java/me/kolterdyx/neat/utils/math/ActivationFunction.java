package me.kolterdyx.neat.utils.math;

import org.ejml.simple.SimpleMatrix;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ActivationFunction {

    NONE,
    SIGMOID,
    RELU,
    STEP;

    private static final Map<String, ActivationFunction> MAP = Stream.of(ActivationFunction.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

    public static ActivationFunction fromName(String name){
        return MAP.get(name);
    }

    public double calculate(double x){
        switch (this){
            case SIGMOID: return 1/(1+Math.pow(Math.E, -x));
            case RELU: return x > 0 ? x : 0;
            case STEP: return x > 0 ? 1 : 0;
            case NONE: return x;
        }
        return 0d;
    }

    public SimpleMatrix calculateMatrix(SimpleMatrix matrix){
        SimpleMatrix result = new SimpleMatrix(matrix.numRows(), matrix.numCols());
        for (int i=0;i<matrix.getNumElements();i++){
            result.set(i, calculate(matrix.get(i)));
        }
        return result;
    }
}
