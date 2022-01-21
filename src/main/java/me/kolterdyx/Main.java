package me.kolterdyx;

import me.kolterdyx.neat.Network;

public class Main {
    public static void main(String[] args) {
        Network n = new Network(2, 3);
        n.addNode(0, 2);
        n.addNode(1, 2);
        n.addConnection(0, 3);
    }
}
