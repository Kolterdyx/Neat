package me.kolterdyx.neat;

import me.kolterdyx.neat.utils.data.Configuration;
import org.junit.Test;

public class NetworkTest {


    @Test
    public void testMutationAddNode(){
        Configuration config = new Configuration("config.yml");

        Network network = new Network(config.getInt("network.inputs"), config.getInt("network.outputs"), config);
        network.addNode(0, 3);
    }

    @Test
    public void testMutationRemoveNode(){
        Configuration config = new Configuration("config.yml");

        Network network = new Network(config.getInt("network.inputs"), config.getInt("network.outputs"), config);
        network.addNode(9, 11);
        network.removeNode(13);
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
