package org.fenci.ppe.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class SaverLoader {

    public static void saveProject(List<JSONData> data, File saveFolder, String projectName, Gson gson) throws IOException {

        File projectFile = new File(saveFolder.toString(), projectName + ".json");

        try (FileWriter writer = new FileWriter(projectFile)) {
            gson.toJson(data, writer);
        }
    }

    public static List<JSONData> loadProject(File projectFile, Gson gson) {
        List<JSONData> loaded;
        Type type = new TypeToken<List<JSONData>>(){}.getType();
        try (FileReader reader = new FileReader(projectFile)) {
            loaded = gson.fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return loaded;
    }
}
