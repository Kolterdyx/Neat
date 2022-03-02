package me.kolterdyx.neat;

import me.kolterdyx.neat.utils.data.Configuration;
import org.ejml.simple.SimpleMatrix;
import org.junit.*;

import java.io.IOException;

public class NetworkTest {

    @Test
    public void testFeedGoodInputToDevelopedNetwork(){

        // Configuration has 3 inputs and two outputs
        Configuration config = new Configuration("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/config.yml");

        Network network = new Network(config);

        for (int i = 0; i < 10; i++) {
            network.tryMutation();
        }


        SimpleMatrix testInputData = new SimpleMatrix(new double[][]{
                new double[] {2, 1, 3}
        });

        SimpleMatrix expectedOutput = new SimpleMatrix(new double[][]{
                new double[] {1.00874, 1.95091}
        });


        SimpleMatrix actualOutput = network.feed(testInputData);

        assert actualOutput.isIdentical(expectedOutput, 0.00001);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testFeedBadInputToDevelopedNetwork(){
        // Configuration has 3 inputs and two outputs
        Configuration config = new Configuration("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/config.yml");

        Network network = new Network(config);

        for (int i = 0; i < 5; i++) {
            network.tryMutation();
        }


        SimpleMatrix testInputData = new SimpleMatrix(new double[][]{
                new double[] {0, 1}
        });

        network.feed(testInputData);
    }

    @Test
    public void testCopyNetworkAndModify(){
        Configuration config = new Configuration("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/config.yml");

        Network network1 = new Network(config);

        for (int i = 0; i < 5; i++) {
            network1.tryMutation();
        }

        Network network2 = network1.copy();

        assert network1.equals(network2);

        for (int i = 0; i < 5; i++) {
            network2.tryMutation();
        }

        assert !network1.equals(network2);
    }

    @Test
    public void testImportNetworkFromFile() throws IOException {
        Network.importFromFile("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/network.json");
    }

    @Test
    public void testExportNetworkToFile() throws IOException {
        Configuration config = new Configuration("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/config.yml");

        Network network = new Network(config);

        for (int i = 0; i < 5; i++) {
            network.tryMutation();
        }

        network.exportToFile("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/network.json");
    }

    @Test
    public void testNetworkGivesTheSameOutputAfterExportingAndImportingFromFile() throws IOException {
        // Configuration has 3 inputs and two outputs
        Configuration config = new Configuration("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/config.yml");

        Network originalNetwork = new Network(config);

        for (int i = 0; i < 20; i++) {
            originalNetwork.tryMutation();
        }

        originalNetwork.exportToFile("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/network2.json");
        Network importedNetwork = Network.importFromFile("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/network2.json");

        SimpleMatrix testInputData = new SimpleMatrix(new double[][]{
                new double[] {2, 1, 3}
        });

        SimpleMatrix expectedOutput = originalNetwork.feed(testInputData);
        SimpleMatrix actualOutput = importedNetwork.feed(testInputData);

        assert expectedOutput.isIdentical(actualOutput, 0.00001);

    }

    @Test
    public void testDisplayGraph(){
        // NOTE: I'm not really sure about how to do this since the graph runs on a different thread, and it's not
        // really accessible outside the graph class.
    }

}
