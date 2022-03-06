package me.kolterdyx.neat.genome;

import me.kolterdyx.neat.genome.Genome;
import me.kolterdyx.utils.Configuration;
import org.junit.Test;

public class GenomeTest {


    @Test
    public void testMutationAddNode(){
        Configuration config = new Configuration("/home/kolterdyx/IdeaProjects/Neat/src/test/resources/config.yml");

        Genome genome = new Genome(config.getInt("network.inputs"), config.getInt("network.outputs"), config);
        genome.addNode(0, 3);
    }

    @Test
    public void testMutationRemoveNode(){
        Configuration config = new Configuration("/home/kolterdyx/IdeaProjects/Neat/src/test/resources/config.yml");

        Genome genome = new Genome(config.getInt("network.inputs"), config.getInt("network.outputs"), config);
        genome.addNode(9, 11);
        genome.removeNode(13);
    }

    @Test
    public void testMutationAddConnection(){

    }

    @Test
    public void testMutationRemoveConnection(){

    }

    @Test
    public void testMutationChangeWeight(){

    }

}
