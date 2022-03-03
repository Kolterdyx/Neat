package me.kolterdyx.neat.utils;

import me.kolterdyx.neat.Network;
import me.kolterdyx.utils.Configuration;
import me.kolterdyx.utils.Experimental;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

import static me.kolterdyx.neat.utils.Signal.*;

@Experimental
public class PopulationThread {

    private volatile HashMap<Long, Network> population;
    private volatile HashMap<Long, double[]> results;
    private volatile HashMap<Long, double[]> oldResults;
    private volatile ArrayList<Long> idList;
    private volatile ArrayList<Long> availableChildren;

    private Configuration config;
    private static long idCounter=0;

    private Thread processorThread;

    private volatile HashMap<Long, Long> familyTree;

    private long lastOffer=-1;


    private volatile ArrayList<Pair<Signal, Object>> inputQueue;
    private volatile boolean newDataAvailable;

    private HashMap<Long, double[]> input;
    private volatile String string;

    public PopulationThread(Configuration config, int initialPopulation){
        population = new HashMap<>();
        results = new HashMap<>();
        idList = new ArrayList<>();
        availableChildren = new ArrayList<>();
        familyTree = new HashMap<>();
        input = new HashMap<>();
        processorThread = new Thread(this::listen);
        this.config = config;

        inputQueue = new ArrayList<>();
        inputQueue.add(new Pair<>(NULL, NULL));

        for (int i = 0; i < initialPopulation; i++) {
            long id = addNetwork(-1);
            input.put(id, new double[config.getInt("network.inputs")]);
        }

        createString();
    }

    public void listen(){

        boolean running = true;
        while (running){

            if (newDataAvailable){
                Signal signal = inputQueue.get(0).getKey();
                Object data = inputQueue.get(0).getValue();
                inputQueue.remove(0);

                if (signal==null) continue;

                switch (signal){
                    case MUTATE_ID -> {
                        int id = ((int[]) data)[0];
                        int times = ((int[]) data)[1];
                        for (int i = 0; i < times; i++) {
                            mutate(id);
                        }
                    }
                    case MUTATE_ALL -> {
                        int times = (int) data;
                        for (int i = 0; i < times; i++) {
                            mutateAll();
                        }
                    }
                    case PREPARE_CHILD_FOR_ID -> {
                        long id = (long) data;
                        addChildNetwork(id);
                    }
                    case INPUT_DATA -> {
                        input = (HashMap<Long, double[]>) data;
                    }
                    case CLOSE -> {
//                        System.out.println("Closing population thread");
                        running = false;
                    }
                }
            }

            feedAll(input);

            createString();

        }
    }

    public void sendData(Signal signal, Object data){
        this.inputQueue.add(new Pair<>(signal, data));
        this.newDataAvailable = true;
    }

    public HashMap<Long, double[]> getResults(){
        try {
            oldResults = results;
            return results;
        } catch (ConcurrentModificationException e){
            e.printStackTrace();
            return oldResults;
        }
    }

    public HashMap<Long, Long> getFamilyTree(){
        return familyTree;
    }

    public long getParent(long childId){
        return familyTree.get(childId);
    }

    public String serialize(){
        // TODO: add serialization
        return null;
    }

    private void feedAll(HashMap<Long, double[]> data){
        if (data == null) return;
        data.forEach(this::feed);
    }

    private void feed(long key, double[] value){
        results.put(key, population.get(key).feed(value));
    }

    public void mutate(long id){
        population.get(id).tryMutation();
    }

    public void mutateAll(){
        population.forEach((key, value) -> value.tryMutation());
    }

    public long addNetwork(long parentId){
        long childId = idCounter++;
        storeNetwork(childId, parentId, new Network(config));
        return childId;
    }

    public long addChildNetwork(long parentId){
        long childId = idCounter++;
        Network parent = population.get(parentId);
        Network child = parent.copy();
        child.tryMutation();
        storeNetwork(childId, parentId, child);
        return childId;
    }

    private void storeNetwork(long id, long parentId, Network network){
        population.put(id, network);
        idList.add(id);
        familyTree.put(id, parentId);
    }

    public ArrayList<Long> getIdList(){
        return idList;
    }

    public void start(){
        processorThread.start();
    }
    
    public void interrupt(){
        processorThread.interrupt();
    }

    public void join(){
        try {
            processorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public HashMap<Long, Network> getPopulation() {
        return population;
    }


    @Override
    public String toString() {
        return string;
    }

    public void createString(){
        string = "";
        population.forEach((key, value) -> {
            string += "["+key+"]"+value+"\n";
        });
    }
}
