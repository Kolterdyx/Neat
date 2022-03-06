package me.kolterdyx;

import me.kolterdyx.neat.Network;
import me.kolterdyx.neat.utils.network.InnovationRegistry;
import me.kolterdyx.neat.utils.network.Species;
import me.kolterdyx.utils.Configuration;

import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Configuration config = new Configuration("config.yml");
        Species.setConfig(config);
        InnovationRegistry.setConfig(config);

        Network a = new Network(config);

        for (int i = 0; i < 10; i++) {
            a.tryMutation();
        }
        Network b = a.copy();
        for (int i = 0; i < 10; i++) {
            a.tryMutation();
            b.tryMutation();
        }

        Network c = new Network(config);
        c.setGenome(Species.crossover(a, b));
        Network d = new Network(config);
        d.setGenome(Species.crossover(b, a));

        System.out.println(Species.geneticDistance(a,b));
        System.out.println(Species.geneticDistance(a,c));
        System.out.println(Species.geneticDistance(a,d));

        FileWriter file1 = new FileWriter("a.json");
        FileWriter file2 = new FileWriter("b.json");
        FileWriter file3 = new FileWriter("c.json");
        FileWriter file4 = new FileWriter("d.json");
        file1.write(a.serialize());
        file2.write(b.serialize());
        file3.write(c.serialize());
        file4.write(d.serialize());
        file1.close();
        file2.close();
        file3.close();
        file4.close();


//        System.out.println(Species.geneticDistance(a, b));

    }
}
