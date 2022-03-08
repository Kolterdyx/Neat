package me.kolterdyx;

import me.kolterdyx.neat.Network;
import me.kolterdyx.neat.utils.network.InnovationRegistry;
import me.kolterdyx.neat.utils.network.Species;
import me.kolterdyx.utils.Configuration;

public class Main {

    public static void main(String[] args) {
        Configuration config = new Configuration("config.yml");
        InnovationRegistry.setConfig(config);
        Species.setConfig(config);

        Network a = new Network(config);

        for (int i = 0; i < 10; i++) {
            a.tryMutation();
        }
        Network b = a.copy();
        for (int i = 0; i < 10; i++) {
            a.tryMutation();
            b.tryMutation();
        }

        Network c = Species.crossover(a, b);
        Network d = Species.crossover(b, a);
    }
}
