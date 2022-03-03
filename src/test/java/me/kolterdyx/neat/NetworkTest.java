package me.kolterdyx.neat;

import me.kolterdyx.utils.Configuration;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;

public class NetworkTest {

    @Test
    public void testFeedGoodInputToDevelopedNetwork(){

        // Configuration has 3 inputs and two outputs
        Configuration config = new Configuration("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/config.yml");

        Network network = new Network(config);

        for (int i = 0; i < 10; i++) {
            network.tryMutation();
        }


        double[] testInputData = new double[] {2, 1, 3};

        double[] expectedOutput = new double[] {1.00874, 1.95091};


        double[] actualOutput = network.feed(testInputData);

        assert Arrays.equals(actualOutput, expectedOutput);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testFeedBadInputToDevelopedNetwork(){
        // Configuration has 3 inputs and two outputs
        Configuration config = new Configuration("/home/kolterdyx/Almacenamiento/Ciro/Projects/Neat/src/test/resources/config.yml");

        Network network = new Network(config);

        for (int i = 0; i < 5; i++) {
            network.tryMutation();
        }


        var testInputData = new double[] {0, 1};

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

        double[] testInputData = new double[] {2, 1, 3};

        double[] expectedOutput = originalNetwork.feed(testInputData);
        double[] actualOutput = importedNetwork.feed(testInputData);

        assert Arrays.equals(expectedOutput, actualOutput);

    }

    @Test
    public void testDisplayGraph(){
        // NOTE: I'm not really sure about how to do this since the graph runs on a different thread, and it's not
        // really accessible outside the graph class.
    }

}
