package me.kolterdyx.neat.utils.neural;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serializer {
    private static final Gson j = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public static String serialize(Object o){
        return j.toJson(o);
    }

    public static <T> T deserialize(String json, Class<T> c){
        return j.fromJson(json, c);
    }
}
