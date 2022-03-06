package me.kolterdyx.neat.genome;

import me.kolterdyx.utils.Configuration;
import org.junit.Test;

public class GenomeTest {


    @Test
    public void testMutationAddNode(){
        Configuration config = new Configuration("/home/kolterdyx/IdeaProjects/Neat/src/test/resources/config.yml");

        Genome genome = new Genome(config);
        genome._addNode(0, 3);
    }

    @Test
    public void testMutationRemoveNode(){
        Configuration config = new Configuration("/home/kolterdyx/IdeaProjects/Neat/src/test/resources/config.yml");

        Genome genome = new Genome(config);
        genome._addNode(9, 11);
        genome._removeNode(13);
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
