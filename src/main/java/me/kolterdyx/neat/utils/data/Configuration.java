package me.kolterdyx.neat.utils.data;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Configuration {

    @Expose
    private String filePath;
    @Expose
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

    private Object getData(String path){
        String[] paths = path.split("\\.");
        Object result = data.get(paths[0]);
        if (paths.length == 1) return result;
        for (int i = 1; i < paths.length; i++) {
            result = ((Map<String, Object>) result).get(paths[i]);
        }
        return result;
    }

    public Number getNumber(String path){
        return (Number) getData(path);
    }

    public int getInt(String path){
        Number n = getNumber(path);
        if (n instanceof Double){
            return Integer.parseInt(""+(int)(double)n);
        }
        return (int)getNumber(path);
    }

    public float getFloat(String path){
        return (float)getDouble(path);
    }
    public double getDouble(String path){
        Number n = getNumber(path);
        if (n instanceof Integer){
            return Double.parseDouble(""+(int)n);
        }
        return (double)getNumber(path);
    }
    public long getLong(String path){
        return (long)getNumber(path);
    }
    public boolean getBoolean(String path){
        return (boolean)getData(path);
    }
    public List<?> getList(String path){
        return (List<?>) getData(path);
    }

    public String getString(String path){
        return (String)getData(path);
    }

}
