package me.kolterdyx.neat.genome;

import com.google.gson.annotations.Expose;
import me.kolterdyx.utils.Configuration;
import me.kolterdyx.neat.utils.data.ActivationFunction;
import me.kolterdyx.neat.utils.network.GeneKey;
import me.kolterdyx.neat.utils.network.InnovationRegistry;

import java.util.*;

public class Genome {

    private Configuration config;
    @Expose
    private final HashMap<Integer, Node> nodes = new HashMap<>();
    @Expose
    private final HashMap<Integer, Connection> connections = new HashMap<>();
    @Expose
    private final int inputs;
    @Expose
    private final int outputs;

    private transient Random random;

    @Expose
    private final int[] inputNodeIDs;
    @Expose
    private final int[] outputNodeIDs;

    public Genome(Configuration config){
        this.config = config;

        this.inputs = config.getInt("network.inputs");
        this.outputs = config.getInt("network.outputs");

        random = new Random();
        if (config.getBoolean("network.random.use-seed")) {
            random.setSeed(config.getInt("network.random.seed"));
        }

        inputNodeIDs = new int[inputs];
        outputNodeIDs = new int[outputs];

        // Input nodes
        for (int i = 0; i < inputs; i++) {
            Node node = new Node(Node.INPUT, i, ActivationFunction.fromName(config.getString("network.input-activation")), random, config.getDouble("network.bias-range"));
            inputNodeIDs[i] = node.getInnovation();
            _registerGene(node);
        }

        // Output nodes
        for (int i = 0; i < outputs; i++) {
            Node node = new Node(Node.OUTPUT, inputs+i, ActivationFunction.fromName(config.getString("network.output-activation")), random, config.getDouble("network.bias-range"));
            outputNodeIDs[i] = node.getInnovation();
            _registerGene(node);
        }
    }

    public void _setConfig(Configuration config){
        this.config = config;
    }

    public double[] _feed(double[] data){
        if (data==null) return null;

        if (data.length != inputs) {
            throw new IllegalArgumentException("Input data array has a different length ("+data.length+") than expected "+inputs);
        }

        // Reset all nodes
        for (Node node : nodes.values()){
            node.reset();
        }

        for (int i=0; i<inputs; i++){
            double value = data[i];
            Node node = nodes.get(inputNodeIDs[i]);
            node.addInputValue(value);
        }

        for (Node node : nodes.values()){
            calculateNode(node);
        }

        double[] values = new double[outputs];

        for (int i = 0; i < outputs; i++) {
            values[i] = nodes.get(outputNodeIDs[i]).getOutput();
        }


        return values;
    }

    private boolean recurrent(GeneKey con){
        int inputNodeInn = con.a();
        int outputNodeInn = con.b();
        if (!nodes.containsKey(inputNodeInn) || !nodes.containsKey(outputNodeInn)) return false;

        if (inputNodeInn==outputNodeInn) return true;
        else if (nodes.get(inputNodeInn).getNodeType() == Node.INPUT || nodes.get(outputNodeInn).getNodeType() == Node.OUTPUT) return false;

        ArrayList<int[]> connectionList = new ArrayList<>();

        for (Connection c : connections.values()){
            connectionList.add(new int[]{c.getInputNode(), c.getOutputNode()});
        }

        ArrayList<Integer> visited = new ArrayList<>();
        visited.add(outputNodeInn);
        int timeout=0;
        while (true){
            timeout++;
            if (timeout>=config.getInt("network.while-true-timeout")) {
                System.out.println("Recurrent check timed out. Returning true");
                return true;
            }
            int n=0;
            for (int[] c : connectionList){
                int a = c[0];
                int b = c[1];
                if (visited.contains(a) && !visited.contains(b)){
                    if (b == inputNodeInn) return true;
                    visited.add(b);
                    n++;
                }
            }
            if (n == 0) return false;
        }
    }

    private double calculateNode(Node node){

        if (node.hasBeenProcessed()) return node.getOutput();

        if (node.getNodeType() == Node.INPUT){
            node.calculate();
            return node.getOutput();
        }
        node.setNumberOfInputs(0);
        node.reset();
        for (int conInn : node.getIncomingConnections()){
            Connection con = connections.get(conInn);
            if (con.enabled()) node.addNewInput();
        }

        if (node.getNumberOfInputs()==0){
            node.calculate();
            return node.getOutput();
        }

        for (int conInn : node.getIncomingConnections()){
            Connection con = connections.get(conInn);
            if (con.enabled()) {
                if (!nodes.get(con.getInputNode()).hasBeenProcessed()) {
                    double value = calculateNode(nodes.get(con.getInputNode())) * con.getWeight();
                    node.addInputValue(value);
                } else {
                    node.addInputValue(nodes.get(con.getInputNode()).getOutput() * con.getWeight());
                }
            }
        }
        node.calculate();
        return node.getOutput();
    }

    public void _registerGene(Gene gene) {
        if (gene instanceof Node node){
            if (nodes.containsKey(node.getInnovation())) return;
            nodes.put(node.getInnovation(), node);
        } else if (gene instanceof Connection con){
            if (connections.containsKey(con.getInnovation())) return;
            connections.put(con.getInnovation(), con);
            _registerGene(con.getInputNodeInstance());
            _registerGene(con.getOutputNodeInstance());
        }
    }

    public boolean _addNode(int prevNode, int nextNode){
        GeneKey conKey = new GeneKey(prevNode, nextNode);
        if (recurrent(conKey)) return false;
        // Disable existing connection between the wrapping nodes if it exists
        if (InnovationRegistry.connectionExists(conKey)){
            Connection con = connections.get(InnovationRegistry.getConnection(prevNode, nextNode));
            con.disable();
            nodes.get(con.getOutputNode()).removeInput();
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
        Node node = new Node(Node.HIDDEN, InnovationRegistry.getNode(prevNode, nextNode), ActivationFunction.fromName(config.getString("network.hidden-activation")), random, config.getDouble("network.bias-range"));
        _registerGene(node);
        // We create two connections surrounding the node and connecting it to the previous and next nodes.
        _addConnection(prevNode, node.getInnovation());
        _addConnection(node.getInnovation(), nextNode);

        return true;
    }

    public boolean _addConnection(int inputNode, int outputNode) {

        // the wrapping nodes must exist
        if (!nodes.containsKey(inputNode) || !nodes.containsKey(outputNode)) return false;
        // the wrapping nodes must not be equal
        else if (inputNode == outputNode) return false;
        // the wrapping nodes must not be both input or output nodes simultaneously
        else if (nodes.get(inputNode).getNodeType() == nodes.get(outputNode).getNodeType() && // nodes are the same type
                nodes.get(inputNode).getNodeType() != Node.HIDDEN) return false; // they are not hidden nodes

        GeneKey conKey = new GeneKey(inputNode, outputNode);
        if (recurrent(conKey)) return false;
        if (InnovationRegistry.connectionExists(conKey)){
            Connection con = connections.get(InnovationRegistry.getConnection(conKey));
            if (con != null && !con.enabled()){
                con.enable();
                return true;
            }
        }

        Connection con = new Connection(nodes.get(inputNode), nodes.get(outputNode), random, config.getDouble("network.weight-range"));


        nodes.get(outputNode).addIncomingConnection(con.getInnovation());
        _registerGene(con);

        return true;
    }

    public void _removeNode(int node) {
        // TODO: remove nodes
    }

    public void _mutateWeight() {
        if (connections.size()==0) return;
        Connection con = (Connection) connections.values().toArray()[random.nextInt(connections.size())];
        double weightLimit = config.getDouble("network.weight-range");
        con.setWeight(con.getWeight()+random.nextDouble(weightLimit*2)-weightLimit);
    }

    public void _mutateBias(){
        Node node = (Node) nodes.values().toArray()[random.nextInt(nodes.size())];
        double biasLimit = config.getDouble("network.bias-range");
        node.setBias(node.getBias()+random.nextDouble(biasLimit*2)-biasLimit);
    }

    public void _addRandomNode() {
        if (connections.size() == 0){
            _addRandomConnection();
        }

        Connection connectionToSplit = (Connection) connections.values().toArray()[random.nextInt(connections.size())];

        int timeout=0;
        while (!connectionToSplit.enabled()) {
            timeout++;
            if (timeout>config.getInt("network.while-true-timeout")){
                return;
            }
            connectionToSplit = (Connection) connections.values().toArray()[random.nextInt(connections.size())];
        }
        _addNode(connectionToSplit.getInputNode(), connectionToSplit.getOutputNode());

    }

    public void _removeRandomNode() {
        Node node = (Node) nodes.values().toArray()[random.nextInt(nodes.size())];
        _removeNode(node.getInnovation());
    }

    public void _addRandomConnection() {
        int inNode = -1;
        int outNode = -1;
        if (nodes.size() == inputs+outputs){
            inNode = inputNodeIDs[random.nextInt(inputs)];
            outNode = outputNodeIDs[random.nextInt(outputs)];
            _addConnection(inNode, outNode);
        } else {

            // Choose inNode
            if (random.nextBoolean()){
                // hidden as inNode
                int choice = -1;
                int t=0;
                while (choice == -1 || nodes.get(choice).getNodeType() != Node.HIDDEN){
                    t++;
                    if (t>=10000) {
                        break;
                    }
                    choice = ((Node) nodes.values().toArray()[random.nextInt(nodes.size())]).getInnovation();
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
                int t=0;
                while (choice == -1 || nodes.get(choice).getNodeType() != Node.HIDDEN || choice == inNode){
                    t++;
                    if (t>=10000) {
                        break;
                    }
                    choice = ((Node) nodes.values().toArray()[random.nextInt(nodes.size())]).getInnovation();
                }
                outNode = choice;
            } else {
                // output as outNode
                outNode = outputNodeIDs[random.nextInt(outputs)];
            }
            _addConnection(inNode, outNode);
        }
    }

    public void _removeRandomConnection() {
        if (connections.size() == 0) return;
        ArrayList<Connection> cons = new ArrayList<>();
        for (Connection con : connections.values()){
            if (con.enabled()) cons.add(con);
        }
        if (cons.size() == 0) return;
        Connection con = cons.get(random.nextInt(cons.size()));
        con.disable();
    }

    public ArrayList<Connection> _getConnections() {
        ArrayList<Connection> conList = new ArrayList<>(connections.values());
        Collections.sort(conList);
        return conList;
    }

    public void _setRandom(Random r){
        this.random = r;
    }

    @Override
    public String toString() {
        return "\n -|"+nodes+"\n -|"+connections+"\n";
    }


}
