package me.kolterdyx.neat;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.genome.Connection;
import me.kolterdyx.neat.genome.Gene;
import me.kolterdyx.neat.genome.Node;
import me.kolterdyx.neat.utils.Configuration;
import me.kolterdyx.neat.utils.Experimental;
import me.kolterdyx.neat.utils.graph.MouseListener;
import me.kolterdyx.neat.utils.math.ActivationFunction;
import me.kolterdyx.neat.utils.neural.GeneKey;
import me.kolterdyx.neat.utils.neural.InnovationRegistry;
import org.bytedeco.libfreenect._freenect_device;
import org.ejml.simple.SimpleMatrix;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;

import java.util.*;

public class Network {

    private transient SingleGraph graph;

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
    private String graphStyle = """
                node {
                    text-size: 12px;
                    text-color: white;
                    text-alignment: center;
                    shape: freeplane;
                    size-mode: fit;
                }
                node.input {
                    fill-color: blue;
                }
                node.output {
                    fill-color: red;
                }
                node.middle {
                    fill-color: gray;
                }
                
                graph {
                    fill-color: #0f0f0f;
                }
                
                edge {
                    fill-color: #909090;
                    shape: freeplane;
                    text-size: 12px;
                    text-color: white;
                    text-alignment: center;
                }
                edge.positive {
                    fill-color: #228a2e;
                }
                edge.negative {
                    fill-color: #821d16;
                }
                edge.disabled {
                    fill-color: rgba(0, 0, 0, 0);
                }
                """;

    public Network(int inputs, int outputs, Configuration config){

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


    public SimpleMatrix feed(SimpleMatrix data){
        // Reset all nodes
        for (Node node : nodes.values()){
            node.reset();
        }

        for (int i=0; i <inputs; i++){
            double value = data.get(i);
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


        return new SimpleMatrix(new double[][]{
                values
        });
    }

    private boolean recurrent(GeneKey con){
        int i = con.getIn();
        int o = con.getOut();

        if (i==o) return true;
        else if (nodes.get(i).getNodeType() == Node.INPUT || nodes.get(o).getNodeType() == Node.OUTPUT) return false;

        ArrayList<int[]> conList = new ArrayList<>();

        for (Connection c : connections.values()){
            conList.add(new int[]{c.getInputNode(), c.getOutputNode()});
        }

        ArrayList<Integer> visited = new ArrayList<>();
        visited.add(o);

        while (true){
            int n=0;
            for (int[] c : conList){
                int a = c[0], b = c[1];
                if (visited.contains(a) && !visited.contains(b)){
                    if (b == i) return true;
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

        for (int conInn : node.getIncomingConnections()){
            Connection con = connections.get(conInn);
            if (con.enabled()) {
                if (!nodes.get(con.getInputNode()).hasBeenProcessed()) {
                    double value = calculateNode(nodes.get(con.getInputNode())) * con.getWeight();
                    node.addInputValue(value);
                } else {
                    node.addInputValue(nodes.get(con.getInputNode()).getOutput());
                }
            } else {
                node.addInputValue(0);
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
    }

    public void addRandomNode() {
        if (connections.size() == 0){
            addRandomConnection();
        }

        Connection conToSplit = (Connection) connections.values().toArray()[random.nextInt(connections.size())];

        while (!conToSplit.enabled()) conToSplit = (Connection) connections.values().toArray()[random.nextInt(connections.size())];
        addNode(conToSplit.getInputNode(), conToSplit.getOutputNode());

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
                while (choice == -1 || nodes.get(choice).getNodeType() != Node.HIDDEN || choice == inNode){
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

    Viewer viewer = null;


    @Experimental
    public void plotGraph(){

        if (viewer == null) {
            viewer = graph.display();
            viewer.getDefaultView().setSize(1280, 720);
        }

        for (Connection con : connections.values()){
            int in = con.getInputNode();
            int out = con.getOutputNode();
            String inName = ""+in;
            String outName = ""+out;
            graph.addNode(inName);
            graph.addNode(outName);
            Edge e;
            e = graph.addEdge("" + con.getInnovation(), inName, outName, true);
//            if (!con.enabled() && config.getBoolean("network.graph.display-disabled-links") || con.enabled()) {
//            } else return;
//            if (e==null) return;
            e.setAttribute("ui.style", "shape: blob;");
            double width = Math.abs(con.getWeight()*3)+0.1f;
            e.setAttribute("ui.style", "size: "+width+"px;");
            if (con.getWeight() > 0){
                e.setAttribute("ui.class", "positive");
            } else{
                e.setAttribute("ui.class", "negative");
            }
            if (!con.enabled()){
                e.setAttribute("ui.class", "disabled");
            }
            if (con.enabled() && config.getBoolean("network.graph.display-link-info")) e.setAttribute("ui.label", ""+con);
            if (config.getBoolean("network.graph.display-node-info")) graph.getNode(inName).setAttribute("ui.label", ""+nodes.get(in));
            if (config.getBoolean("network.graph.display-node-info")) graph.getNode(outName).setAttribute("ui.label", ""+nodes.get(out));
            if (Arrays.stream(inputNodeIDs).anyMatch(Integer.valueOf(in)::equals)){
                graph.getNode(inName).setAttribute("ui.class", "input");
            }else {
                graph.getNode(inName).setAttribute("ui.class", "middle");
            }

            if (Arrays.stream(outputNodeIDs).anyMatch(Integer.valueOf(out)::equals)){
                graph.getNode(outName).setAttribute("ui.class", "output");
            }else{
                graph.getNode(outName).setAttribute("ui.class", "middle");
            }
        }
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
        return ""+nodes+"\n"+connections;
    }

    void createGraph() {
        graph = new SingleGraph("Network");
        graph.setStrict(false);

        graph.setAutoCreate(true);
        System.setProperty("org.graphstream.ui", "swing");
//        System.setProperty("org.graphstream.ui", "javafx");
        graph.setAttribute("ui.stylesheet", graphStyle);
    }

    public Graph getGraph(){
        return graph;
    }
}
