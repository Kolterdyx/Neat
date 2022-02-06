package me.kolterdyx;

import me.kolterdyx.neat.Neat;
import me.kolterdyx.neat.utils.Configuration;
import org.ejml.simple.SimpleMatrix;

public class Main {
    public static void main(String[] args) {
        var n = new Neat(new Configuration("config.yml"));
        for (int i = 0; i < 50; i++) {
            n.tryMutation();
        }
        System.out.println(n.feed(new SimpleMatrix(new double[][]{
                new double[]{-2, -2}
        })));
    }
}
