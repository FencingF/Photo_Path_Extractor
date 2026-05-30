package org.fenci.ppe.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.fenci.ppe.data.JSONData;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {

        List<JSONData> plottableData = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter project name: ");
        String projectName = scanner.nextLine();

        String rootPath = System.getProperty("user.dir");
        File saveFolder = new File(rootPath + "\\src\\main\\java\\org\\fenci\\ppe\\data\\savedprojects");
        File projectFile = new File(saveFolder, projectName + ".json");

        Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

        if (projectFile.exists() && projectFile.isFile()) {
            plottableData = loadProject(projectFile, gson);
            for (JSONData dataa : plottableData) {
                System.out.println(dataa.fileName() + "  " + dataa.date() + " " + dataa.latitude() + " " + dataa.longitude());
            }
            System.out.println("Already exists");

        } else {
            Date date = new Date();
            JSONData saveData = new JSONData("name1", date, 33.9695, -118.4165);
            JSONData saveData2 = new JSONData("name2", date, 33.9695, -118.4165);
            plottableData.add(saveData);
            plottableData.add(saveData2);
            saveProject(plottableData, saveFolder, projectName, gson);
        }
    }

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
