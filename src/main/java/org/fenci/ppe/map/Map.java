package org.fenci.ppe.map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Map {

    private final List<Coordinate> points;
    private final JXMapViewer mapViewer;
    private final WaypointPainter<ColoredWaypoint> waypointPainter;

    public Map() {
        points = new ArrayList<>();
        mapViewer = new JXMapViewer();
        waypointPainter = new WaypointPainter<>();

        mapViewer.setHorizontalWrapped(false);
        mapViewer.setTileFactory(createSatelliteTileFactory());

        GeoPosition defaultLocation = new GeoPosition(33.9695, -118.4165);

        mapViewer.setAddressLocation(defaultLocation);
        mapViewer.setZoom(4);

        waypointPainter.setRenderer((graphics, map, waypoint) -> {

            Point2D point = map.getTileFactory().geoToPixel(waypoint.getPosition(), map.getZoom());

            int x = (int) point.getX();
            int y = (int) point.getY();

            graphics.setColor(waypoint.getColor());
            graphics.fillOval(x - 7, y - 7, 14, 14);

            graphics.setColor(Color.BLACK);
            graphics.drawOval(x - 7, y - 7, 14, 14);

            graphics.setFont(new Font(waypoint.getFont(), Font.BOLD, 14));

            String label = waypoint.getLabel();

            if (label != null && !label.isEmpty()) {
                FontMetrics metrics = graphics.getFontMetrics();

                int textWidth = metrics.stringWidth(label);

                int textX = x - textWidth / 2;
                int textY = y - 14;

                graphics.setColor(Color.BLACK);
                graphics.drawString(label, textX + 1, textY + 1);

                graphics.setColor(Color.WHITE);
                graphics.drawString(label, textX, textY);
            }
        });

        mapViewer.setOverlayPainter(waypointPainter);

        MouseInputListener mouseInputListener = new PanMouseInputListener(mapViewer);

        mapViewer.addMouseListener(mouseInputListener);
        mapViewer.addMouseMotionListener(mouseInputListener);

        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
    }

    public void addPoint(double latitude, double longitude, Color color, String label, String font) {
        points.add(new Coordinate(latitude, longitude, color, label, font));
        updateMarkers();
    }

    public void addPoint(Coordinate coordinate) {
        points.add(coordinate);
        updateMarkers();
    }

    public void clearPoints() {
        points.clear();
        updateMarkers();
    }

    public void centerOn(double latitude, double longitude) {
        mapViewer.setAddressLocation(new GeoPosition(latitude, longitude));
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Satellite Map");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.add(mapViewer, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }

    private void updateMarkers() {
        Set<ColoredWaypoint> waypoints = new HashSet<>();

        for (Coordinate point : points) {
            waypoints.add(new ColoredWaypoint(point.getLatitude(), point.getLongitude(), point.getColor(), point.getLabel(), point.getFont()));
        }

        waypointPainter.setWaypoints(waypoints);
        mapViewer.repaint();
    }

    public void drawArrow(Coordinate from, Coordinate to) {
        mapViewer.setOverlayPainter((g, map, w, h) -> {
            Graphics2D g2 = (Graphics2D) g.create();

            Rectangle viewport = map.getViewportBounds();

            g2.translate(-viewport.getX(), -viewport.getY());

            Point2D fromPoint = map.getTileFactory().geoToPixel(new GeoPosition(from.getLatitude(), from.getLongitude()), map.getZoom());

            Point2D toPoint = map.getTileFactory().geoToPixel(new GeoPosition(to.getLatitude(), to.getLongitude()), map.getZoom());

            int x1 = (int) fromPoint.getX();
            int y1 = (int) fromPoint.getY();
            int x2 = (int) toPoint.getX();
            int y2 = (int) toPoint.getY();

            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(3));

            g2.drawLine(x1, y1, x2, y2);
            drawArrowHead(g2, x1, y1, x2, y2);

            g2.dispose();

            waypointPainter.paint(g, map, w, h);
        });

        mapViewer.repaint();
    }

    private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);

        int arrowLength = 18;

        int xA = (int) (x2 - arrowLength * Math.cos(angle - Math.PI / 6));
        int yA = (int) (y2 - arrowLength * Math.sin(angle - Math.PI / 6));

        int xB = (int) (x2 - arrowLength * Math.cos(angle + Math.PI / 6));
        int yB = (int) (y2 - arrowLength * Math.sin(angle + Math.PI / 6));

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(x2, y2);
        arrowHead.addPoint(xA, yA);
        arrowHead.addPoint(xB, yB);

        g2.fillPolygon(arrowHead);
    }

    private DefaultTileFactory createSatelliteTileFactory() {
        TileFactoryInfo info = new TileFactoryInfo(1, 17, 17, 256, true, true, "https://server.arcgisonline.com", "x", "y", "z") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                int z = getTotalMapZoom() - zoom;

                return "https://server.arcgisonline.com/ArcGIS/rest/services/" + "World_Imagery/MapServer/tile/" + z + "/" + y + "/" + x;
            }
        };

        return new DefaultTileFactory(info);
    }
}