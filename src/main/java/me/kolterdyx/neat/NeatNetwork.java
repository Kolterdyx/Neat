package me.kolterdyx.neat;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.genome.Connection;
import me.kolterdyx.neat.genome.Gene;
import me.kolterdyx.neat.genome.Node;
import me.kolterdyx.neat.utils.neural.GeneKey;
import me.kolterdyx.neat.utils.neural.InnovationRegistry;
import me.kolterdyx.neat.utils.neural.Serializer;
import org.ejml.simple.SimpleMatrix;

import java.util.HashMap;

public class NeatNetwork implements Network {


    @Expose
    private HashMap<Integer, Node> nodes = new HashMap<>();
    @Expose
    private HashMap<Integer, Connection> connections = new HashMap<>();
    @Expose
    private int inputs;
    @Expose
    private int outputs;

    public NeatNetwork(int inputs, int outputs){

        this.inputs = inputs;
        this.outputs = outputs;

        for (int i = 0; i < inputs; i++) {
            Node node = new Node(Node.INPUT, InnovationRegistry.getOuterNode());
            registerGene(node);
        }

        for (int i = 0; i < outputs; i++) {
            Node node = new Node(Node.OUTPUT, InnovationRegistry.getOuterNode());
            registerGene(node);
        }

    }


    public SimpleMatrix feed(SimpleMatrix data){
        // TODO: implement node calculation

        return null;
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
    }

    public void removeRandomNode() {
    }

    public void addRandomConnection() {
    }

    public void removeRandomConnection() {
    }
}
