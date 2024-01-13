import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.swing.Timer;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class CityPlotter {

    private ArrayList<Map<String, String>> csvDataList;

    // Constructor to receive CSV file path
    public CityPlotter(String csvFilePath) {
        this.csvDataList = readCSVData(csvFilePath);
    }

    public void visualizeGraph() {
        // Create JUNG graph
        Graph<String, String> graph = createGraph();

        // Print graph information
        printGraphInformation(graph);

        // Create JUNG layout using ISOMLayout
        Layout<String, String> layout = new ISOMLayout<>(graph);

        // Create visualization viewer
        VisualizationViewer<String, String> vv = new VisualizationViewer<>(layout, new Dimension(800, 600));

        // Customize vertex labels
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<>());

        // Add mouse listener to display data on hover
        vv.setVertexToolTipTransformer(vertex -> getVertexTooltip(vertex));

        vv.getRenderContext().setVertexFillPaintTransformer(vertex -> Color.BLUE);

        showGraph(vv);

        // Animate the graph layout
        animateGraphLayout(vv);
    }

    private String getVertexTooltip(String cityName) {
        // Retrieve additional data from the CSV based on the city name
        for (Map<String, String> rowData : csvDataList) {
            if (rowData.get("City").equals(cityName)) {
                // Customize this part based on your CSV structure
                return "<html><b>City:</b> " + cityName +
                        "<br><b>Distance:</b> " + rowData.get("Distance") +
                        "<br><b>Weather:</b> " + rowData.get("Weather") +
                        "<br><b>Gallons:</b> " + rowData.get("Gallons") +
                        "<br><b>ETA:</b> " + rowData.get("ETA") +
                        "<br><b>Risk Factor:</b> " + rowData.get("Risk_Factor") +
                        "</html>";
            }
        }
        return null; // Return null if city data is not found
    }

    private void animateGraphLayout(VisualizationViewer<String, String> vv) {
        ScalingControl scaler = new CrossoverScalingControl();
        DefaultModalGraphMouse<String, String> graphMouse = new DefaultModalGraphMouse<>();
        graphMouse.setMode(Mode.TRANSFORMING);
        vv.setGraphMouse(graphMouse);
        vv.getRenderContext().setMultiLayerTransformer(vv.getRenderContext().getMultiLayerTransformer());

        Timer timer = new Timer(100, e -> {
            scaler.scale(vv, 1.03f, vv.getCenter());
            vv.repaint();
        });

        timer.setRepeats(true);
        timer.start();
    }

    private void showGraph(VisualizationViewer<String, String> vv) {
        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void printGraphInformation(Graph<String, String> graph) {
        System.out.println("Graph Information:");

        for (String vertex : graph.getVertices()) {
            System.out.println("City: " + vertex);
        }
    }

    private Graph<String, String> createGraph() {
        Graph<String, String> graph = new DirectedSparseGraph<>();
        String previousName = "";
        for (Map<String, String> rowData : csvDataList) {
            String cityName = rowData.get("City");

            // Customize this part based on your CSV structure
            graph.addVertex(cityName);
            if (previousName != "") {
                graph.addEdge(cityName + " -> " + previousName, previousName, cityName);
            }
            previousName = cityName;
            // You can also add edges here if needed
        }

        return graph;
    }

    private ArrayList<Map<String, String>> readCSVData(String csvFilePath) {
        ArrayList<Map<String, String>> dataList = new ArrayList<>();
        ArrayList<String> headersOrder = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                if (headersOrder.isEmpty()) {
                    // Assuming the first line contains headers
                    while (tokenizer.hasMoreTokens()) {
                        String header = tokenizer.nextToken().trim();
                        headersOrder.add(header);
                    }
                } else {
                    String[] values = new String[headersOrder.size()];
                    int i = 0;
                    while (tokenizer.hasMoreTokens()) {
                        values[i++] = tokenizer.nextToken().trim();
                    }

                    Map<String, String> rowData = new LinkedHashMap<>(); // Using LinkedHashMap to preserve insertion
                    // order
                    for (int j = 0; j < headersOrder.size(); j++) {
                        rowData.put(headersOrder.get(j), values[j]);
                    }
                    dataList.add(rowData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    public static void main(String[] args) {
        // Provide the path to your CSV file
        String csvFilePath = "C:\\Users\\PNW_checkout\\Documents\\Algo\\project\\Milestone4\\city_attributes.csv";
        CityPlotter visualizer = new CityPlotter(csvFilePath);
        visualizer.visualizeGraph();
    }
}
