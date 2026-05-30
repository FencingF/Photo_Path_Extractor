package org.fenci.ppe;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.fenci.ppe.data.JSONData;
import com.drew.metadata.exif.GpsDirectory;
import org.fenci.ppe.map.Coordinate;
import org.fenci.ppe.map.Map;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static org.fenci.ppe.data.SaverLoader.loadProject;
import static org.fenci.ppe.data.SaverLoader.saveProject;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException, ImageProcessingException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter project name: ");
        String projectName = scanner.nextLine();

        String rootPath = System.getProperty("user.dir");
        File saveFolder = new File(rootPath + "\\src\\main\\java\\org\\fenci\\ppe\\data\\savedprojects");
        File projectFile = new File(saveFolder, projectName + ".json");

        Map interMap = new Map();
//        interMap.addPoint(new Coordinate(33.9695, -118.4165, Color.GREEN, "fsdfsd", "Arial"));
        interMap.display();
//        interMap.addPoint(new Coordinate(34, -118.4165, Color.RED, "GAY", "Arial"));
        File photoFolder = new File("C:\\Users\\jeanf\\Pictures\\iCloud Photos\\Photos");

        try {
            List<JSONData> plottableData;

            //TODO: PUT JSON CODE HERE
            Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

            if (projectFile.exists() && projectFile.isFile()) {
                plottableData = loadProject(projectFile, gson);
                System.out.println("Already exists");

            } else {
                plottableData = getMapData(photoFolder);
                saveProject(plottableData, saveFolder, projectName, gson);
            }

            //TODO: END JSON CODE

            plottableData.sort(
                    Comparator.comparing(
                            JSONData::date
                    )
            );
            long milliDelay = 1000L / plottableData.size();
            for (JSONData data : plottableData) {
                System.out.println(data.date() + " " + data.latitude() + " " + data.longitude());
                SwingUtilities.invokeLater(() -> interMap.addPoint(new Coordinate(data.latitude(), data.longitude(), Color.ORANGE, data.date().toString(), "Arial")));
                Thread.sleep(milliDelay);
            }
        } catch (NullPointerException e) {
            System.out.println("Fix your code lil bro");
        }
    }

    public static List<JSONData> getMapData(File photoFolder) {
        File[] files = photoFolder.listFiles();

        if (files == null) {
            System.out.println("Folder not found.");
            return null;
        }

        final int limit = 100; //Amount of pictures downloaded at a time.
        int processed = 0;
        int index = 0;

        ArrayList<JSONData> mapData = new ArrayList<>();
        ArrayList<File> filesToDelete = new ArrayList<>();

        for (File file : files) {
            index++;
            if (file.isFile() && isPicture(file)) {

                processed++;

                try {
                    Metadata metadata = ImageMetadataReader.readMetadata(file);
                    GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);

                    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                    filesToDelete.add(file);

                    if (gps == null || gps.getGeoLocation() == null || gps.getGeoLocation().isZero()) {
                        System.out.println(file.getName() + " had no GPS data :(");
                        continue;
                    }
                    System.out.println(file.getName() + " had GPS data! :)");

                    GeoLocation loc = gps.getGeoLocation();
                    Date dateTaken = null;
                    if (directory != null) {
                        dateTaken = directory.getDateOriginal();
                    }

                    if (dateTaken != null) {
                        mapData.add(new JSONData(file.getName(), dateTaken, loc.getLatitude(), loc.getLongitude()));
                    }
                } catch (Exception e) {
                    System.out.println(file.getName() + " could not be processed.");
                }

                if (processed >= limit) {
                    unloadFile(filesToDelete);
                    processed = 0;
                    filesToDelete.clear();
                }
            }
            if (index % 25 == 0) {
                System.out.println("Went through " + index + " photos");
            }
        }
        if (!filesToDelete.isEmpty()) { //After the loop if the files are not all deleted they are now
            unloadFile(filesToDelete);
        }
        return mapData;
    }

    public static boolean isPicture(File file) {
//        file.getName().endsWith(".JPG") || file.getName().endsWith(".PNG") || file.getName().endsWith(".JPEG") ||
        return file.getName().endsWith(".HEIC");
    }

    public static void unloadFile(List<File> filesToBeUnloaded) {
        try {
            for (File file : filesToBeUnloaded) {
                Process process = new ProcessBuilder("attrib", "+U", file.getAbsolutePath()).start();

                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    System.out.println("Failed: " + file.getName());
                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public static void saveToJSON(JSONData data) {
        //SAVE DATA TO JSON FIRST AND THEN LOAD IT TO MAP FROM THE JSON
    }
}