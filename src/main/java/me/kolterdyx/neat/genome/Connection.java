package me.kolterdyx.neat.genome;

import com.google.gson.annotations.Expose;
import me.kolterdyx.neat.utils.InnovationRegistry;

public class Connection extends Gene {
    @Expose
    private double weight;
    @Expose
    private final int inputNode;
    @Expose
    private final int outputNode;
    @Expose
    private boolean enabled;

    public Connection(int inputNode, int outputNode){
        GENE_TYPE = Gene.CONNECTION;
        this.innovation = InnovationRegistry.getConnection(inputNode, outputNode);
        this.inputNode = inputNode;
        this.outputNode = outputNode;
        this.weight = Double.parseDouble(String.format("%.5f", Math.random()));
        enable();
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean enabled() {
        return enabled;
    }
}
