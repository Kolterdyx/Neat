package me.kolterdyx.neat.utils.neural;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.kolterdyx.neat.Neat;

public class Serializer {
    private static Gson j = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public static String serialize(Object o){
        return j.toJson(o);
    }

    public static Neat deserialize(String json){
        Neat a = j.fromJson(json, Neat.class);
        a.postProcess();
        return a;
    }
}
