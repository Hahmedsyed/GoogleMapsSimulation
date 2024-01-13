import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class AnimatedCityGraph extends JFrame {
    private Map<String, Point> cities = new HashMap<>();
    private Map<String, Map<String, Double>> distances = new HashMap<>();
    private Map<String, Color> stateColors = new HashMap<>();
    private String[] stateNames;

    private double zoomFactor = 1.0;

    public AnimatedCityGraph() {
        setTitle("Animated City Graph Visualization");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Read data from CSV and populate 'cities' and 'distances' maps
        readDataFromCSV("Final_Connection_V3.csv");

        // Get unique state names
        stateNames = distances.keySet().stream().map(city -> city.split("_")[0]).distinct().toArray(String[]::new);

        // Assign different colors to each state
        assignColorsToStates();

        //printDistances();

        GraphPanel graphPanel = new GraphPanel();
        setContentPane(graphPanel);

        // Add MouseWheelListener for zooming
        addMouseWheelListener(new ZoomListener());

        // Set the preferred size based on the calculated positions of cities
        Dimension preferredSize = calculatePreferredSize();
        graphPanel.setPreferredSize(preferredSize);

        // Pack the frame to adjust its size
        pack();
    }

    private void assignColorsToStates() {
        Color[] colors = {
            Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.PINK,
            Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.LIGHT_GRAY, 
            Color.DARK_GRAY, Color.GRAY, Color.BLACK, Color.WHITE, 
            new Color(255, 165, 0), new Color(128, 0, 128), new Color(0, 128, 0),
            new Color(255, 0, 0), new Color(0, 255, 0), new Color(0, 0, 255),
            new Color(255, 255, 0), new Color(255, 0, 255), new Color(0, 255, 255)
        };

        if (stateNames.length > colors.length) {
            System.out.println("Warning: Not enough colors for all states");
        }

        for (int i = 0; i < stateNames.length; i++) {
            stateColors.put(stateNames[i], colors[i % colors.length]);
        }
    }

    private void readDataFromCSV(String csvFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // Skip the header row
                }

                String[] parts = line.split(",");
                String originState = parts[0].trim();
                String destinationState = parts[1].trim();
                String originCity = parts[2].trim();
                String destinationCity = parts[3].trim();

                String cityName = originState + "_" + originCity;
                String destinationName = destinationState + "_" + destinationCity;
                int x = (int) (Math.random() * 1500) + 50; // Assign a random x-coordinate
                int y = (int) (Math.random() * 1500) + 50; // Assign a random y-coordinate
                Point cityLocation = new Point(x, y);

                cities.put(cityName, cityLocation);

                double distance = Double.parseDouble(parts[4].trim());

                // Create a new Map for distances if not present
                distances.putIfAbsent(cityName, new HashMap<>());

                // Add the distance to the destination city
                distances.get(cityName).put(destinationName, distance);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private Dimension calculatePreferredSize() {
        int maxX = 0, maxY = 0;
        for (Point cityLocation : cities.values()) {
            maxX = Math.max(maxX, cityLocation.x);
            maxY = Math.max(maxY, cityLocation.y);
        }
        return new Dimension((int) (maxX * zoomFactor), (int) (maxY * zoomFactor));
    }

    private class GraphPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.scale(zoomFactor, zoomFactor);

            // Draw edges (distances)
            for (Map.Entry<String, Map<String, Double>> entry : distances.entrySet()) {
                String cityName = entry.getKey();
                Map<String, Double> cityDistances = entry.getValue();
                for (Map.Entry<String, Double> neighborEntry : cityDistances.entrySet()) {
                    String neighborCity = neighborEntry.getKey();
                    drawEdge(g2d, cityName, neighborCity);
                }
            }

            // Draw cities (nodes)
            for (Map.Entry<String, Point> entry : cities.entrySet()) {
                String cityName = entry.getKey();
                Point cityLocation = entry.getValue();
                drawCity(g2d, cityName, cityLocation);
            }
        }
    }

    private void drawEdge(Graphics2D g, String city1, String city2) {
        Point p1 = cities.get(city1);
        Point p2 = cities.get(city2);

        // Null check for city distances
        if (p1 != null && p2 != null && distances.containsKey(city1) && distances.get(city1).containsKey(city2)) {
            g.setColor(Color.BLACK);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);

            // Print distance on the line
            double distance = distances.get(city1).get(city2);
            int midX = (p1.x + p2.x) / 2;
            int midY = (p1.y + p2.y) / 2;
            g.drawString(String.format("%.2f miles", distance), midX, midY);
        } else {
            System.err.println("Distance information not found for city pair: " + city1 + " and " + city2);
        }
    }

    private void drawCity(Graphics2D g, String cityName, Point cityLocation) {
        int diameter = 30;

        // Get the state name from the city name
        String stateName = cityName.split("_")[0];

        // Set color based on state
        g.setColor(stateColors.get(stateName));
        g.fillOval(cityLocation.x - diameter / 2, cityLocation.y - diameter / 2, diameter, diameter);

        // Draw city name
        g.setColor(Color.BLACK);
        g.drawString(cityName, cityLocation.x - 10, cityLocation.y - 40);
    }

    private void saveGraphAsPNG(String fileName) {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        paint(g);

        try {
            ImageIO.write(image, "png", new File(fileName));
            System.out.println("Graph saved as " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            g.dispose();
        }
    }

    private class ZoomListener implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int notches = e.getWheelRotation();
            if (notches < 0) {
                // Zoom in
                zoomFactor *= 1.1;
            } else {
                // Zoom out
                zoomFactor /= 1.1;
            }
            repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AnimatedCityGraph cityGraph = new AnimatedCityGraph();
            cityGraph.setVisible(true);

            // Save the graph as PNG
            cityGraph.saveGraphAsPNG("animated_city_graph.png");
        });
    }
}
