package me.kolterdyx.neat;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.genome.Connection;
import me.kolterdyx.neat.genome.Gene;
import me.kolterdyx.neat.genome.Node;
import me.kolterdyx.neat.utils.Configuration;
import me.kolterdyx.neat.utils.neural.GeneKey;
import me.kolterdyx.neat.utils.neural.InnovationRegistry;
import me.kolterdyx.neat.utils.neural.Serializer;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class NeatNetwork implements Network {


    private final Configuration config;
    @Expose
    private HashMap<Integer, Node> nodes = new HashMap<>();
    @Expose
    private HashMap<Integer, Connection> connections = new HashMap<>();
    @Expose
    private int inputs;
    @Expose
    private int outputs;

    private Random random;

    private int[] inputNodeIDs;
    private int[] outputNodeIDs;

    public NeatNetwork(int inputs, int outputs, Configuration config){

        this.inputs = inputs;
        this.outputs = outputs;
        this.config = config;

        random = new Random();
        if (config.getBoolean("network.random.use-seed")) {
            random.setSeed(config.getInt("network.random.seed"));
        }

        inputNodeIDs = new int[inputs];
        outputNodeIDs = new int[outputs];

        // Input nodes
        for (int i = 0; i < inputs; i++) {
            Node node = new Node(Node.INPUT, InnovationRegistry.getOuterNode());
            inputNodeIDs[i] = node.getInnovation();
            registerGene(node);
        }

        // Output nodes
        for (int i = 0; i < outputs; i++) {
            Node node = new Node(Node.OUTPUT, InnovationRegistry.getOuterNode());
            outputNodeIDs[i] = node.getInnovation();
            registerGene(node);
        }

    }


    public SimpleMatrix feed(SimpleMatrix data){
        // Reset all nodes
        for (Node node : nodes.values()){
            node.reset();
        }

        // Calculate input nodes
        for (int i=0; i <inputs; i++){
            double value = data.get(i);
            Node node = nodes.get(inputNodeIDs[i]);
            node.addInputValue(value);
            node.calculate();
        }

        // Extract hidden nodes
        ArrayList<Node> hiddenNodes = new ArrayList<>();
        for (Node node : nodes.values()){
            if (node.getNodeType() == Node.HIDDEN){
                hiddenNodes.add(node);
            }
        }



        // Calculate hidden nodes
        if (nodes.size() > inputs+outputs){
            int nodesToCalculate = nodes.size() - (inputs + outputs);
            while (nodesToCalculate > 0){
                System.out.println(nodesToCalculate);
                for (Node node: hiddenNodes){
                    for (Connection con : connections.values()){
                        if (con.enabled()){
                            if (con.getOutputNode() == node.getInnovation()){
                                if (nodes.get(con.getInputNode()).hasBeenProcessed()){
                                    node.addInputValue(nodes.get(con.getInputNode()).getOutput() * con.getWeight());
                                } else {
                                    // recurrent calculation
                                }
                            }
                        }
                    }

                    if (!node.hasBeenProcessed()){
                        node.calculate();
                        nodesToCalculate--;
                    }

                }
            }
        }

        // Calculate output nodes

        for (int nodeInn : outputNodeIDs){
            for (Connection con : connections.values()){
                if (con.getOutputNode() == nodeInn){
                    nodes.get(nodeInn).addInputValue(nodes.get(con.getOutputNode()).getOutput() * con.getWeight());
                }
            }
            nodes.get(nodeInn).calculate();
        }


        double[] values = new double[outputs];

        for (int i = 0; i < outputs; i++) {
            values[i] = nodes.get(outputNodeIDs[i]).getOutput();
        }


        return new SimpleMatrix(new double[][]{
                values
        });
    }

    private boolean recurrent(GeneKey con){

        // TODO: prevent recurrent connections

        return false;
    }


    private void registerGene(Gene gene) {
        if (gene instanceof Node node){
            nodes.put(node.getInnovation(), node);
        } else if (gene instanceof Connection con){
            connections.put(con.getInnovation(), con);
        }
    }


    public boolean addNode(int prevNode, int nextNode){
        // Disable existing connection between the wrapping nodes if it exists
        if (InnovationRegistry.connectionExists(new GeneKey(prevNode, nextNode))){
            System.out.println("yeet");
            connections.get(InnovationRegistry.getConnection(prevNode, nextNode)).disable();
        }

        // the wrapping nodes must exist
        if (!nodes.containsKey(prevNode) || !nodes.containsKey(nextNode)) return false;
        // the wrapping nodes must not be equal
        else if (prevNode == nextNode) return false;
        // the wrapping nodes must not be both input or output nodes simultaneously
        else if (nodes.get(prevNode).getNodeType() == nodes.get(nextNode).getNodeType() && // nodes are the same type
                nodes.get(prevNode).getNodeType() != Node.HIDDEN && // they are not hidden nodes
                nodes.get(prevNode).getNodeType() != Node.OUTPUT) return false; // first node is not an output node

        // We create a new node with an innovation number.
        // We create two connections surrounding the node and connecting it to the previous and next nodes.
        Node node = new Node(Node.HIDDEN, InnovationRegistry.getNode(prevNode, nextNode));
        Connection input = new Connection(prevNode, node.getInnovation());
        Connection output = new Connection(node.getInnovation(), nextNode);
        registerGene(node);
        registerGene(input);
        registerGene(output);

        return true;
    }

    public boolean addConnection(int inputNode, int outputNode) {

        // the wrapping nodes must exist
        if (!nodes.containsKey(inputNode) || !nodes.containsKey(outputNode)) return false;
        // the wrapping nodes must not be equal
        else if (inputNode == outputNode) return false;
        // the wrapping nodes must not be both input or output nodes simultaneously
        else if (nodes.get(inputNode).getNodeType() == nodes.get(outputNode).getNodeType() && // nodes are the same type
                nodes.get(inputNode).getNodeType() != Node.HIDDEN) return false; // they are not hidden nodes

        GeneKey conKey = new GeneKey(inputNode, outputNode);
        if (InnovationRegistry.connectionExists(conKey)){
            Connection con = connections.get(InnovationRegistry.getConnection(conKey));
            if (con != null && !con.enabled()){
                con.enable();
                return true;
            }
        }

        Connection con = new Connection(inputNode, outputNode);
        registerGene(con);

        return true;
    }

    public String serialize(){
        return Serializer.serialize(this);
    }

    public void mutateWeight() {
    }

    public void addRandomNode() {
        if (connections.size() == 0){
            addRandomConnection();
        }

        System.out.println(connections);

        Connection conToSplit = connections.get(connections.keySet().);
        conToSplit.disable();

        Node node =  new Node(Node.HIDDEN, InnovationRegistry.getNode(conToSplit.getInputNode(), conToSplit.getOutputNode()));

        addConnection(conToSplit.getInputNode(), node.getInnovation());
        addConnection(node.getInnovation(), conToSplit.getOutputNode());

    }

    public void removeRandomNode() {
    }

    public void addRandomConnection() {
        int inNode = -1;
        int outNode = -1;
        if (nodes.size() == inputs+outputs){
            inNode = inputNodeIDs[random.nextInt(inputs)];
            outNode = outputNodeIDs[random.nextInt(outputs)];
            addConnection(inNode, outNode);
        } else {

            // Choose inNode
            if (random.nextBoolean()){
                // hidden as inNode
                int choice = -1;
                while (choice == -1 || nodes.get(choice).getNodeType() != Node.HIDDEN){
                    choice = nodes.get(random.nextInt(nodes.keySet().size())).getInnovation();
                }
                inNode = choice;
            } else {
                // input as inNode
                inNode = inputNodeIDs[random.nextInt(inputs)];
            }

            // Choose outNode
            if (random.nextBoolean()){
                // hidden as outNode
                int choice = -1;
                while (choice == -1 || nodes.get(choice).getNodeType() != Node.HIDDEN || choice == inNode){
                    choice = nodes.get(random.nextInt(nodes.keySet().size())).getInnovation();
                }
                outNode = choice;
            } else {
                // output as outNode
                outNode = outputNodeIDs[random.nextInt(outputs)];
            }
            addConnection(inNode, outNode);
        }
    }

    public void removeRandomConnection() {
    }
}
