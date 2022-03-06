package me.kolterdyx.neat.utils.network;

import me.kolterdyx.neat.Network;
import me.kolterdyx.neat.genome.Connection;
import me.kolterdyx.neat.genome.Genome;
import me.kolterdyx.utils.Configuration;


public class Species {

    private static Configuration config;


    /**
     * Calculates the genetic distance between two genomes
     *
     * @author Finn Eggers (https://github.com/Luecx)
     * @param genomeA Genome with higher fitness
     * @param genomeB Genome with lower fitness
     * @return Genetic distance from genomeA to genomeB
     */
    public static double geneticDistance(Genome genomeA, Genome genomeB) {
        int highest_innovation_gene1 = 0;
        if (genomeA._getConnections().size()!=0){
            highest_innovation_gene1 = genomeA._getConnections().get(genomeA._getConnections().size()-1).getInnovation();
        }
        int highest_innovation_gene2 = 0;
        if (genomeB._getConnections().size()!=0){
            highest_innovation_gene2 = genomeB._getConnections().get(genomeB._getConnections().size()-1).getInnovation();
        }

        Genome g1;
        Genome g2;

        if (highest_innovation_gene1 < highest_innovation_gene2){
            g1 = genomeB;
            g2 = genomeA;
        } else {
            g1 = genomeA;
            g2 = genomeB;
        }


        int index_g1 = 0;
        int index_g2 = 0;

        int matching = 0;
        int disjoint = 0;
        int excess;

        double weightDiff = 0;

        while (index_g1 < g1._getConnections().size() && index_g2 < g2._getConnections().size()) {
            Connection gene1 = g1._getConnections().get(index_g1);
            Connection gene2 = g2._getConnections().get(index_g2);

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
        excess = g1._getConnections().size() - index_g1;

        double N = Math.max(g1._getConnections().size(), g2._getConnections().size());
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

    /**
     * Method overloading that calls geneticDistance(Genome, Genome) with the genomes of the networks passed
     *
     * @param a Network with higher fitness
     * @param b Network with lower fitness
     * @return Genetic distance from a to b
     */
    public static double geneticDistance(Network a, Network b){ return geneticDistance(a._getGenome(), b._getGenome()); }

    /**
     * Matching genes are inherited randomly
     * Disjoint genes are all inherited
     * Excess genes are only inherited from Parent A
     *
     * @author Finn Eggers (https://github.com/Luecx)
     * @param g1 Parent A (higher fitness)
     * @param g2 Parent B (lower fitness)
     * @return Combination of the genomes g1 and g2
     */
    public static Genome crossover(Genome g1, Genome g2){

        Genome child = new Genome(config);

        int index_g1 = 0;
        int index_g2 = 0;

        while (index_g1 < g1._getConnections().size() && index_g2 < g2._getConnections().size()){

            Connection gene1 = g1._getConnections().get(index_g1);
            Connection gene2 = g2._getConnections().get(index_g2);

            int in1 = gene1.getInnovation();
            int in2 = gene2.getInnovation();

            if (in1==in2){
                // Matching gene
                if(Math.random() > 0.5){
                    child._registerGene(gene1);
                }else{
                    child._registerGene(gene2);
                }
                index_g1++;
                index_g2++;
            } else if (in1 > in2) {
                // Disjoint gene of B
                child._registerGene(gene2);
                index_g2++;
            } else {
                // Disjoint gene of A
                child._registerGene(gene1);
                index_g1++;
            }
        }

        // Excess genes will be only inherited from parent A
        while (index_g1 < g1._getConnections().size()){
            Connection gene = g1._getConnections().get(index_g1);
            child._registerGene(gene);
            index_g1++;
        }

        return child;
    }

    /**
     * Method overloading that calls crossover(Genome, Genome) with the genomes of the networks passed,
     * and then stores the child genome into a new Network
     *
     * @param n1 Network with higher fitness
     * @param n2 Network with lower fitness
     * @return Combination of the networks n1 and n2
     */
    public static Network crossover(Network n1, Network n2){
        Network child = new Network(config);
        child._setGenome(Species.crossover(n1._getGenome(), n2._getGenome()));
        return child;
    }

}
