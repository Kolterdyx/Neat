package me.kolterdyx.neat.utils.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.kolterdyx.neat.Network;

public class Serializer {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public static String _serialize(Network net){
        return gson.toJson(net);
    }

    public static Network _deserialize(String json){
        Network a = gson.fromJson(json, Network.class);
        a._postProcess();
        return a;
    }
}
