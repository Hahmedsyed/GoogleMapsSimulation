import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class nearest_neighbors {

    private Map<String, Map<String, Double>> graph;

    public nearest_neighbors(List<String[]> data) {
        graph = new HashMap<>();

        for (String[] entry : data) {
            String city1 = entry[2].trim().toLowerCase().replaceAll("\\s", "");
            String city2 = entry[3].trim().toLowerCase().replaceAll("\\s", "");
            double distance = Double.parseDouble(entry[4]);

            graph.computeIfAbsent(city1, k -> new HashMap<>()).put(city2, distance);
            graph.computeIfAbsent(city2, k -> new HashMap<>()).put(city1, distance);
        }
    }

    public List<String> findNearestCities(String startCity, int k) {
        Map<String, Double> distances = new HashMap<>();
        PriorityQueue<String> minHeap = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        distances.put(startCity, 0.0);
        minHeap.offer(startCity);

        while (!minHeap.isEmpty()) {
            String currentCity = minHeap.poll();

            if (graph.containsKey(currentCity)) {
                for (Map.Entry<String, Double> neighbor : graph.get(currentCity).entrySet()) {
                    String nextCity = neighbor.getKey();
                    double newDistance = distances.get(currentCity) + neighbor.getValue();

                    if (!distances.containsKey(nextCity) || newDistance < distances.get(nextCity)) {
                        distances.put(nextCity, newDistance);
                        minHeap.offer(nextCity);
                    }
                }
            }
        }

        List<String> nearestCities = new ArrayList<>(distances.keySet());
        nearestCities.remove(startCity);

        // Sort by distance and return the first k cities
        nearestCities.sort(Comparator.comparingDouble(distances::get));

        List<String> result = new ArrayList<>();
        for (String city : nearestCities) {
            double distance = distances.get(city);
            result.add(String.format("%s --> %s, Distance: %.1f miles", startCity, city, distance));
        }

        return result.subList(0, Math.min(k, result.size()));
    }

    public static List<String[]> readDataFromCSV(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();
        boolean firstLine = true;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(",");
                data.add(values);
            }
        }
        return data;
    }

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter the starting city: ");
            String startCity = scanner.nextLine().trim().toLowerCase().replaceAll("\\s", "");
            
            System.out.print("Enter the starting state: ");
            String startState = scanner.nextLine().trim().toLowerCase().replaceAll("\\s", "");

            System.out.print("Enter the number of nearest cities to find: ");
            int k = scanner.nextInt();

            String filePath = "Final_Connection_V3.csv";
            List<String[]> data = readDataFromCSV(filePath);
            nearest_neighbors nearest_neighbors = new nearest_neighbors(data);

            List<String> nearestCities = nearest_neighbors.findNearestCities(startCity, k);

            System.out.println("Nearest cities from " + startCity +"("+startState+ ")"+":"  );
            for (String cityInfo : nearestCities) {
                System.out.println(cityInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
