package org.fenci.ppe;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.fenci.ppe.data.JSONData;
import com.drew.metadata.exif.GpsDirectory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException, ImageProcessingException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter project name: ");
        String projectName = scanner.nextLine();

        //        Map interMap = new Map();
//        interMap.addPoint(new Coordinate(33.9695, -118.4165, Color.GREEN, "fsdfsd", "Arial"));
//        interMap.display();
//        interMap.addPoint(new Coordinate(34, -118.4165, Color.RED, "GAY", "Arial"));
        File photoFolder = new File("C:\\Users\\jeanf\\Pictures\\iCloud Photos\\Photos");

        List<JSONData> plottableData = getMapData(photoFolder);
        if (plottableData != null) {
            for (JSONData data : plottableData) {
                System.out.println(data.date() + " " + data.latitude() + " " + data.longitude());
            }
        }
        System.out.println("Fix your code lil bro");
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
            if (file.isFile()) {
                boolean isLast = (index == files.length - 1);
                if (processed >= limit || isLast) {
                    unloadFile(filesToDelete);
                    processed = 0;
                    filesToDelete.clear();
                }

                try {
                    filesToDelete.add(file);

                    Metadata metadata = ImageMetadataReader.readMetadata(file);
                    GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
                    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

                    GeoLocation loc = gps.getGeoLocation();
                    Date dateTaken = null;
                    if (directory != null) {
                        dateTaken = directory.getDateOriginal();
                    }
                    mapData.add(new JSONData(file.getName(), dateTaken, loc.getLatitude(), loc.getLongitude()));
                    processed++;
                } catch (Exception e) {
                    System.out.println(file.getName() + " could not be processed.");
                }
                index++;
                System.out.println(index);
            }
        }
        return mapData;
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

    public static void saveToJSON() {

    }
}