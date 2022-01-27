package me.kolterdyx.neat.utils;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Configuration {

    private String filePath;
    private Map<String, Object> data;
    public Configuration(String filepath){
        try {
            this.filePath = filepath;
            File file = new File(filepath);
            Scanner scanner = new Scanner(file);
            String fileData = "";
            while (scanner.hasNextLine()){
                fileData += scanner.nextLine() + "\n";
            }

            data = new Yaml().load(fileData);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public int getInt(String path){
        return (int)data.get(path);
    }

    public float getFloat(String path){
        return (float)(int)data.get(path);
    }
    public double getDouble(String path){
        return (double)(int)data.get(path);
    }
    public long getLong(String path){
        return (long)(int)data.get(path);
    }
    public boolean getBoolean(String path){
        return (boolean)data.get(path);
    }
    public List<?> getList(String path){
        return (List<?>) data.get(path);
    }

    public String getString(String path){
        return (String)data.get(path);
    }

}
