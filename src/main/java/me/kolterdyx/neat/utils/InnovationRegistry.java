package me.kolterdyx.neat.utils;

import java.util.HashMap;

public class InnovationRegistry {
    private static int innovationCounter=0;

    private static HashMap<GeneKey, Integer> existingConnections= new HashMap<>();
    private static HashMap<GeneKey, Integer> existingNodes = new HashMap<>();

    public static Integer getConnection(int inNode, int outNode) {

        if (!(existingNodes.containsValue(inNode) && existingNodes.containsValue(outNode))) {
            return null;
        }

        GeneKey connection = new GeneKey(inNode, outNode);
        for (GeneKey i : existingConnections.keySet()) {
            if (i.getIn() == connection.getIn() && i.getOut() == connection.getOut()) return existingConnections.get(i);
        }
        int innovation = getInnovation();
        existingConnections.put(connection, innovation);
        return innovation;

    }
    public static int getConnection(GeneKey conn)  {
        return getConnection(conn.getIn(), conn.getOut());
    }

    // Check if a node already exists between two preexisting nodes, and if so, return that node's innovation number.
    // If not, create a new innovation number and store it with the wrapping preexisting nodes as the key.
    public static Integer getNode(int prevNode, int nextNode) {

        if (!(existingNodes.containsValue(prevNode) && existingNodes.containsValue(nextNode))) {
            return null;
        }

        GeneKey node = new GeneKey(prevNode, nextNode);
        for (GeneKey i : existingNodes.keySet()) {
            if (i.getIn() == node.getIn() && i.getOut() == node.getOut()) return existingNodes.get(i);
        }
        int innovation = getInnovation();
        existingNodes.put(node, innovation);
        return innovation;
    }
    public static int getNode(GeneKey wrap) {
        return getNode(wrap.getIn(), wrap.getOut());
    }


    public static int getOuterNode(){
        GeneKey wrap = new GeneKey(-1, -1);
        int innovation = getInnovation();
        existingNodes.put(wrap, innovation);
        return innovation;
    }

    public static boolean nodeExists(int node){
        return existingNodes.containsValue(node);
    }

    public static boolean nodeExists(GeneKey wrap){
        for (GeneKey i : existingNodes.keySet()) {
            if (i.getIn() == wrap.getIn() && i.getOut() == wrap.getOut()) return true;
        }
        return false;
    }

    public static boolean connectionExists(int connection){
        return existingConnections.containsValue(connection);
    }

    public static boolean connectionExists(GeneKey connection){
        for (GeneKey i : existingConnections.keySet()) {
            if (i.getIn() == connection.getIn() && i.getOut() == connection.getOut()) return true;
        }
        return false;
    }

    private static int getInnovation(){
        return innovationCounter++;
    }

}
