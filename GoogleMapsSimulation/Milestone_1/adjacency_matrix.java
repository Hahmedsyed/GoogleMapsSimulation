import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class adjacency_matrix {

    public static void main(String[] args) {
        String csvFile = "Final_Dataset_Algo.csv"; // Replace with your CSV file path
        String outputTxtFile = "matrix_output.txt"; // Replace with the desired output file path
        String csvDelimiter = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;

            // Read the header line
            br.readLine();

            // Map to store distances between cities
            Map<String, Map<String, Double>> distanceMap = new HashMap<>();

            // Read CSV and create a map of distances
            while ((line = br.readLine()) != null) {
                String[] values = line.split(csvDelimiter);
                String startCity = values[0];
                String endCity = values[1];
                double distance = Double.parseDouble(values[2]);

                // Update the distance map
                distanceMap.computeIfAbsent(startCity, k -> new HashMap<>()).put(endCity, distance);
                distanceMap.computeIfAbsent(endCity, k -> new HashMap<>()).put(startCity, distance);
            }

            // Create and write the adjacency matrix to a text file
            String[] cities = distanceMap.keySet().toArray(new String[0]);
            double[][] adjacencyMatrix = createAdjacencyMatrix(cities, distanceMap);
            writeAdjacencyMatrixToFile(outputTxtFile, cities, adjacencyMatrix);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double[][] createAdjacencyMatrix(String[] cities, Map<String, Map<String, Double>> distanceMap) {
        int numCities = cities.length;
        double[][] adjacencyMatrix = new double[numCities][numCities];

        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                String city1 = cities[i];
                String city2 = cities[j];

                if (distanceMap.containsKey(city1) && distanceMap.get(city1).containsKey(city2)) {
                    adjacencyMatrix[i][j] = distanceMap.get(city1).get(city2);
                } else {
                    adjacencyMatrix[i][j] = 0; // Mark as 0 if cities are not directly connected
                }
            }
        }

        return adjacencyMatrix;
    }

    private static void writeAdjacencyMatrixToFile(String filePath, String[] cities, double[][] adjacencyMatrix) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath)))) {
            writer.print("\t");

            for (String city : cities) {
                writer.print(city + "\t");
            }

            writer.println();

            for (int i = 0; i < cities.length; i++) {
                writer.print(cities[i] + "\t");

                for (int j = 0; j < cities.length; j++) {
                    writer.print(adjacencyMatrix[i][j] + "\t");
                }

                writer.println();
            }

            System.out.println("Adjacency matrix written to: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
