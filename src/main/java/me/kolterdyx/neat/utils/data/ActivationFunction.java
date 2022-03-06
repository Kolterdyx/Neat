package me.kolterdyx.neat.utils.data;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ActivationFunction {

    NONE,
    LINEAR,
    SIGMOID,
    RELU,
    STEP,
    TANH,
    SOFT_TANH;

    private static Map<String, ActivationFunction> MAP = Stream.of(ActivationFunction.values()).collect(Collectors.toMap(Enum::name, Function.identity()));

    public static ActivationFunction fromName(String name){
        return MAP.get(name);
    }

    public double calculate(double x){
        return switch (this) {
            case SIGMOID -> 1 / (1 + Math.pow(Math.E, -x));
            case RELU -> x > 0 ? x : 0;
            case STEP -> x > 0 ? 1 : 0;
            case NONE, LINEAR -> x;
            case TANH -> (Math.pow(Math.E, x) - Math.pow(Math.E, -x)) / (Math.pow(Math.E, x) + Math.pow(Math.E, -x));
            case SOFT_TANH -> TANH.calculate(x / 5);
        };
    }

//    public SimpleMatrix calculateMatrix(SimpleMatrix matrix){
//        SimpleMatrix result = new SimpleMatrix(matrix.numRows(), matrix.numCols());
//        for (int i=0;i<matrix.getNumElements();i++){
//            result.set(i, calculate(matrix.get(i)));
//        }
//        return result;
//    }
}
