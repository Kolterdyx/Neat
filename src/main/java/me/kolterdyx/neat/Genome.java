package me.kolterdyx.neat;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.genome.Connection;
import me.kolterdyx.neat.genome.Gene;
import me.kolterdyx.neat.genome.Node;
import me.kolterdyx.utils.Configuration;
import me.kolterdyx.neat.utils.data.ActivationFunction;
import me.kolterdyx.neat.utils.neural.GeneKey;
import me.kolterdyx.neat.utils.neural.InnovationRegistry;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Genome {

    private Configuration config;
    @Expose
    private HashMap<Integer, Node> nodes = new HashMap<>();
    @Expose
    private HashMap<Integer, Connection> connections = new HashMap<>();
    @Expose
    private int inputs;
    @Expose
    private int outputs;

    private transient Random random;

    @Expose
    private int[] inputNodeIDs;
    @Expose
    private int[] outputNodeIDs;
    private String graphStyle = "";

    public Genome(int inputs, int outputs, Configuration config){

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
            Node node = new Node(Node.INPUT, InnovationRegistry.getOuterNode(), ActivationFunction.fromName(config.getString("network.input-activation")), random, config.getDouble("network.bias-range"));
            inputNodeIDs[i] = node.getInnovation();
            registerGene(node);
        }

        // Output nodes
        for (int i = 0; i < outputs; i++) {
            Node node = new Node(Node.OUTPUT, InnovationRegistry.getOuterNode(), ActivationFunction.fromName(config.getString("network.output-activation")), random, config.getDouble("network.bias-range"));
            outputNodeIDs[i] = node.getInnovation();
            registerGene(node);
        }


        createGraph();

    }

    public void setConfig(Configuration config){
        this.config = config;
    }


    public double[] feed(double[] data){
        if (data==null) return null;
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
        int inputNodeInn = con.getIn();
        int outputNodeInn = con.getOut();
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


    private void registerGene(Gene gene) {
        if (gene instanceof Node node){
            nodes.put(node.getInnovation(), node);
        } else if (gene instanceof Connection con){
            connections.put(con.getInnovation(), con);
        }
    }


    public boolean addNode(int prevNode, int nextNode){
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
        registerGene(node);
        // We create two connections surrounding the node and connecting it to the previous and next nodes.
        addConnection(prevNode, node.getInnovation());
        addConnection(node.getInnovation(), nextNode);

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
        if (recurrent(conKey)) return false;
        if (InnovationRegistry.connectionExists(conKey)){
            Connection con = connections.get(InnovationRegistry.getConnection(conKey));
            if (con != null && !con.enabled()){
                con.enable();
                return true;
            }
        }

        Connection con = new Connection(inputNode, outputNode, random, config.getDouble("network.weight-range"));


        nodes.get(outputNode).addIncomingConnection(con.getInnovation());
        registerGene(con);

        return true;
    }

    public void mutateWeight() {
        if (connections.size()==0) return;
        Connection con = (Connection) connections.values().toArray()[random.nextInt(connections.size())];
        double weightLimit = config.getDouble("network.weight-range");
        con.setWeight(con.getWeight()+random.nextDouble(weightLimit*2)-weightLimit);
    }

    public void mutateBias(){
        Node node = (Node) nodes.values().toArray()[random.nextInt(nodes.size())];
        double biasLimit = config.getDouble("network.bias-range");
        node.setBias(node.getBias()+random.nextDouble(biasLimit*2)-biasLimit);
    }

    public void addRandomNode() {
        if (connections.size() == 0){
            addRandomConnection();
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
        addNode(connectionToSplit.getInputNode(), connectionToSplit.getOutputNode());

    }

    public void removeNode(int node) {
//        throw new NotImplementedException();
    }

    public void removeRandomNode() {
        Node node = (Node) nodes.values().toArray()[random.nextInt(nodes.size())];
        removeNode(node.getInnovation());
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
            addConnection(inNode, outNode);
        }
    }

    public void removeRandomConnection() {
        if (connections.size() == 0) return;
        ArrayList<Connection> cons = new ArrayList<>();
        for (Connection con : connections.values()){
            if (con.enabled()) cons.add(con);
        }
        if (cons.size() == 0) return;
        Connection con = cons.get(random.nextInt(cons.size()));
        con.disable();
    }

    public HashMap<Integer, Node> getNodes() {
        return nodes;
    }

    public HashMap<Integer, Connection> getConnections() {
        return connections;
    }

    public void setRandom(Random r){
        this.random = r;
    }

    @Override
    public String toString() {
        return "\n -|"+nodes+"\n -|"+connections+"\n";
    }

    void createGraph() {

        // Try to find and load stylesheet

        File path = new File(config.getString("network.debug.graph.stylesheet"));
        graphStyle="";
        try {
            Scanner scanner = new Scanner(path);
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                graphStyle = graphStyle + line +"\n";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
