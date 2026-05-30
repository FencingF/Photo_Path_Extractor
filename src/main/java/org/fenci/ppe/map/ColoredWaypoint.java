package org.fenci.ppe.map;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.Color;

public class ColoredWaypoint extends DefaultWaypoint {

    private final Color color;
    private final String label;
    private final String font;

    public ColoredWaypoint(double latitude, double longitude, Color color, String label, String font) {
        super(new GeoPosition(latitude, longitude));
        this.color = color;
        this.label = label;
        this.font = font;
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