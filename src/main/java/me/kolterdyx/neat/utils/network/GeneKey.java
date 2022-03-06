package me.kolterdyx.neat.utils.network;

public class GeneKey {
    private int in;
    private int out;
    public GeneKey(int in, int out){
        this.in = in;
        this.out = out;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GeneKey key){
            return key.getIn() == in && key.getOut() == out;
        }
        return false;
    }

    public int getIn() {
        return in;
    }

    public int getOut() {
        return out;
    }
}
