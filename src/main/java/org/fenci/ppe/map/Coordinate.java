package org.fenci.ppe.map;

import java.awt.*;

public class Coordinate {

    private final double latitude;
    private final double longitude;
    private final Color color;
    private final String label;
    private final String font;

    public Coordinate(double latitude, double longitude, Color color, String label, String font) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
        this.label = label;
        this.font = font;
    }

    public Coordinate(double latitude, double longitude, Color color) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
        this.label = "";
        this.font = "Arial";
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Color getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }

    public String getFont() {
        return font;
    }
}