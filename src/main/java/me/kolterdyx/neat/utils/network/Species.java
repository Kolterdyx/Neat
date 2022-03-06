package me.kolterdyx.neat.utils.network;

import me.kolterdyx.neat.Network;
import me.kolterdyx.neat.genome.Connection;
import me.kolterdyx.neat.genome.Gene;
import me.kolterdyx.neat.genome.Genome;
import me.kolterdyx.neat.genome.Node;
import me.kolterdyx.utils.Configuration;


public class Species {

    private static Configuration config;

    public static double geneticDistance(Genome genA, Genome genB) {


        int highest_innovation_gene1 = 0;
        if (genA.getConnections().size()!=0){
            highest_innovation_gene1 = genA.getConnections().get(genA.getConnections().size()-1).getInnovation();
        }
        int highest_innovation_gene2 = 0;
        if (genB.getConnections().size()!=0){
            highest_innovation_gene2 = genB.getConnections().get(genB.getConnections().size()-1).getInnovation();
        }

        Genome g1;
        Genome g2;

        if (highest_innovation_gene1 < highest_innovation_gene2){
            g1 = genB;
            g2 = genA;
        } else {
            g1 = genA;
            g2 = genB;
        }


        int index_g1 = 0;
        int index_g2 = 0;

        int disjoint = 0;
        int excess = 0;
        int matching = 0;
        double weightDiff = 0;

        while (index_g1 < g1.getConnections().size() && index_g2 < g2.getConnections().size()) {
            Connection gene1 = g1.getConnections().get(index_g1);
            Connection gene2 = g2.getConnections().get(index_g2);

            int in1 = gene1.getInnovation();
            int in2 = gene2.getInnovation();

            if(in1 == in2){
                // Matching gene
                matching++;
                weightDiff += Math.abs(gene1.getWeight() - gene2.getWeight());
                index_g1++;
                index_g2++;
            } else if(in1 > in2){
                // Disjoint gene of b
                disjoint++;
                index_g2++;
            } else{
                // Disjoint gene of a
                disjoint++;
                index_g1++;
            }
        }
        weightDiff /= Math.max(1, matching);
        excess = g1.getConnections().size() - index_g1;

        double N = Math.max(g1.getConnections().size(), g2.getConnections().size());
        if(N < 20){
            N = 1;
        }

        int c1 = config.getInt("network.speciation.c1");
        int c2 = config.getInt("network.speciation.c2");
        int c3 = config.getInt("network.speciation.c3");

        return  c1 * disjoint / N + c2 * excess / N + c3 * weightDiff;
    }

    public static void setConfig(Configuration config){
        Species.config = config;
    }

    public static double geneticDistance(Network a, Network b){ return geneticDistance(a.getGenome(), b.getGenome()); }


    public static Genome crossover(Genome g1, Genome g2){

        Genome child = new Genome(config);

        int index_g1 = 0;
        int index_g2 = 0;

        while (index_g1 < g1.getConnections().size() && index_g2 < g2.getConnections().size()){

            Connection gene1 = g1.getConnections().get(index_g1);
            Connection gene2 = g2.getConnections().get(index_g2);

            int in1 = gene1.getInnovation();
            int in2 = gene2.getInnovation();

            if (in1==in2){
                // Matching gene
                if(Math.random() > 0.5){
                    child.registerGene(gene1);
                }else{
                    child.registerGene(gene2);
                }
                index_g1++;
                index_g2++;
            } else if (in1 > in2) {
                // Disjoint gene of B
                child.registerGene(gene2);
                index_g2++;
            } else {
                // Disjoint gene of A
                child.registerGene(gene1);
                index_g1++;
            }
        }

        // Excess genes will be only inherited from parent A
        while (index_g1 < g1.getConnections().size()){
            Connection gene = g1.getConnections().get(index_g1);
            child.registerGene(gene);
            index_g1++;
        }

        return child;
    }

    public static Genome crossover(Network n1, Network n2){
        return Species.crossover(n1.getGenome(), n2.getGenome());
    }

}
