package me.kolterdyx.neat.utils.neural;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.kolterdyx.neat.Network;

public class Serializer {
    private static Gson j = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public static String serialize(Object o){
        return j.toJson(o);
    }

    public static Network deserialize(String json){
        Network a = j.fromJson(json, Network.class);
        a.postProcess();
        return a;
    }
}
