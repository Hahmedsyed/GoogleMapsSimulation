import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CityGraph extends JFrame {
    private static final int CIRCLE_RADIUS = 10;
    private static final int CITY_SPACING = 80;

    private final List<Point> cityPoints = new ArrayList<>();

    private static class CityDetails {
        String name;
        double distance;
        double gallons;
        String weather;

        public CityDetails(String name, double distance, double gallons, String weather) {
            this.name = name;
            this.distance = distance;
            this.gallons = gallons;
            this.weather = weather;
        }
    }

    private final List<CityDetails> cityDetailsList = new ArrayList<>();

    private double zoomFactor = 1.0;

    public CityGraph() {
        setTitle("City Connections Graph");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addMouseWheelListener(new ZoomHandler());
        setVisible(true);
    }

    public void connectCities(Point p1, Point p2, CityDetails details1, CityDetails details2) {
        Graphics g = getGraphics();
        int x1 = (int) (p1.x * zoomFactor);
        int y1 = (int) (p1.y * zoomFactor);
        int x2 = (int) (p2.x * zoomFactor);
        int y2 = (int) (p2.y * zoomFactor);
        g.drawLine(x1, y1, x2, y2);
        drawArrow(g, x1, y1, x2, y2);
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        g.drawString("Distance: " + details2.distance + " miles", midX, midY - 15);
        g.drawString("Gallons: " + details2.gallons, midX, midY);
        g.drawString("Weather: " + details2.weather, midX, midY + 15);
    }

    private void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
        int arrowSize = 10;
        int dx = x2 - x1;
        int dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int x3 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        int y3 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        int x4 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        int y4 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));

        g.drawLine(x2, y2, x3, y3);
        g.drawLine(x2, y2, x4, y4);
    }

    public void plotCity(Point p, CityDetails cityDetails) {
        Graphics g = getGraphics();
        int x = (int) (p.x * zoomFactor);
        int y = (int) (p.y * zoomFactor);

        g.fillOval(x - CIRCLE_RADIUS / 2, y - CIRCLE_RADIUS / 2, CIRCLE_RADIUS, CIRCLE_RADIUS);
        g.drawString(cityDetails.name, x, y - CIRCLE_RADIUS - 5);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (int i = 1; i < cityPoints.size(); i++) {
            connectCities(cityPoints.get(i - 1), cityPoints.get(i),
                    cityDetailsList.get(i - 1), cityDetailsList.get(i));
        }
        for (int i = 0; i < cityPoints.size(); i++) {
            plotCity(cityPoints.get(i), cityDetailsList.get(i));
        }
    }

    private class ZoomHandler implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int notches = e.getWheelRotation();
            if (notches < 0) {
                zoomIn();
            } else {
                zoomOut();
            }
        }
    }

    private void zoomIn() {
        if (zoomFactor < 2.0) {
            zoomFactor += 0.1;
            updateZoom();
        }
    }

    private void zoomOut() {
        if (zoomFactor > 0.5) {
            zoomFactor -= 0.1;
            updateZoom();
        }
    }

    private void updateZoom() {
        repaint();
    }

    public static void main(String[] args) {
        CityGraph cityGraph = new CityGraph();

        String csvFile = "C:\\Users\\PNW_checkout\\Documents\\Algo\\project\\Milestone3\\city_attributes.csv";
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine();
            int yOffset = 0;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);
                double latitude = Double.parseDouble(data[5]);
                double longitude = Double.parseDouble(data[6]);
                String cityName = data[0];
                double distance = Double.parseDouble(data[2]);
                double gallons = Double.parseDouble(data[3]);
                String weather = data[4];

                cityGraph.cityDetailsList.add(new CityDetails(cityName, distance, gallons, weather));
                cityGraph.cityPoints.add(new Point(
                        (int) ((longitude + 180) * cityGraph.getWidth() / 360),
                        (int) ((90 - latitude) * cityGraph.getHeight() / 180) + yOffset
                ));
                yOffset += CITY_SPACING;
                cityGraph.repaint();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
