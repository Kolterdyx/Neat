package me.kolterdyx.neat.genome;

import com.google.gson.annotations.Expose;

public class Gene {
    public static final int NODE=0;
    public static final int CONNECTION=1;
    @Expose
    protected int GENE_TYPE;
    @Expose
    protected int innovation;


    public int getInnovation() {
        return this.innovation;
    }
}
