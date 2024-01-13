import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class City {
    String name;
    String state;

    public City(String name, String state) {
        this.name = name;
        this.state = state;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        City city = (City) obj;
        return name.equals(city.name) && state.equals(city.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, state);
    }

    @Override
    public String toString() {
        return name;
    }
}

class Edge {
    City source;
    City destination;
    double distance;
    double speed;
    double gradient;

    public Edge(City source, City destination, double distance, double speed, double gradient) {
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.speed = speed;
        this.gradient = gradient;
    }
}

class WeatherData {
    Map<LocalDateTime, String> hourlyConditions;

    public WeatherData() {
        this.hourlyConditions = new HashMap<>();
    }
}

public class ShortestPathFinder {
    private Map<City, List<Edge>> graph;
    private static Map<City, WeatherData> weatherDataMap;

    public ShortestPathFinder() {
        this.graph = new LinkedHashMap<>();
        this.weatherDataMap = new HashMap<>();
    }

    public void addEdge(City source, City destination, double distance, double speed, double gradient) {
        source.state = source.state.isEmpty() ? destination.state : source.state;
        destination.state = destination.state.isEmpty() ? source.state : destination.state;
        graph.computeIfAbsent(source, k -> new ArrayList<>()).add(new Edge(source, destination, distance, speed, gradient));
        graph.computeIfAbsent(destination, k -> new ArrayList<>()).add(new Edge(destination, source, distance, speed, gradient));
    }

    public void readDataFromCSV(String csvFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                String stateOrigin = parts[0].trim().toLowerCase().replaceAll("\\s", "");
                String stateDestination = parts[1].trim().toLowerCase().replaceAll("\\s", "");
                String cityOrigin = parts[2].trim().toLowerCase().replaceAll("\\s", "");
                String cityDestination = parts[3].trim().toLowerCase().replaceAll("\\s", "");
                double distance = Double.parseDouble(parts[4].trim());
                double speed = Double.parseDouble(parts[5].trim());
                double gradient = Double.parseDouble(parts[8].trim());

                addEdge(new City(cityOrigin, stateOrigin), new City(cityDestination, stateDestination), distance, speed, gradient);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace(); // Consider logging or displaying a more user-friendly message.
        }
    }

    public void readWeatherData(String weatherFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(weatherFilePath))) {
            String line;
            boolean firstLine = true;
            String[] firstLineData = null;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLineData = line.split(",");
                    firstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                String cityName = parts[1].trim().toLowerCase().replaceAll("\\s", "");
                String stateName = parts[0].trim().toLowerCase().replaceAll("\\s", "");

                WeatherData weatherData = new WeatherData();

                // Parse date-time and conditions for each hour
                for (int i = 2; i < parts.length - 1; i++) {
                    String dateTimeString = (firstLineData[i]).trim();

                    String conditions = parts[i + 1].trim().toLowerCase().replaceAll("\\s", "");
                    LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("MM/dd/yyyy H:mm", Locale.ENGLISH));

                    weatherData.hourlyConditions.put(dateTime, conditions);
                }
                weatherDataMap.put(new City(cityName, stateName), weatherData);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace(); // Consider logging or displaying a more user-friendly message.
        }
    }

    public Map<City, City> findShortestPaths(City start) {
        Map<City, Double> distances = new HashMap<>();
        Map<City, City> previousNodes = new HashMap<>();
        PriorityQueue<City> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
        Set<City> visited = new HashSet<>();

        distances.put(start, 0.0);
        priorityQueue.add(start);

        while (!priorityQueue.isEmpty()) {
            City current = priorityQueue.poll();

            if (visited.contains(current)) continue;

            visited.add(current);

            for (Edge edge : graph.getOrDefault(current, Collections.emptyList())) {
                City neighbor = edge.destination;
                double newDistance = distances.get(current) + edge.distance;

                if (!distances.containsKey(neighbor) || newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previousNodes.put(neighbor, current);
                    priorityQueue.add(neighbor);
                }
            }
        }

        return previousNodes;
    }

    public List<City> getShortestPath(City start, City destination, Map<City, City> previousNodes) {
        List<City> path = new ArrayList<>();
        City current = destination;

        while (current != null && !current.equals(start)) {
            path.add(current);
            current = previousNodes.get(current);
        }

        if (current != null && current.equals(start)) {
            path.add(start);
            Collections.reverse(path);
        }

        return path;
    }

    public double calculatePathDistance(List<City> path) {
        double distance = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            City current = path.get(i);
            City next = path.get(i + 1);

            if (graph.containsKey(current)) {
                for (Edge edge : graph.get(current)) {
                    if (edge.destination.equals(next)) {
                        distance += edge.distance;
                        break;
                    }
                }
            } else {
                System.out.println("The City name is not present in Data");
                // Handle the case where the graph doesn't contain the current city
            }
        }

        return distance;
    }

    public static void main(String[] args) {
        ShortestPathFinder shortestPathFinder = new ShortestPathFinder();
        shortestPathFinder.readDataFromCSV("Final_Connection_V3.csv");
        shortestPathFinder.readWeatherData("collected Weather.csv");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter source city: ");
        String startCity = scanner.nextLine().toLowerCase().replaceAll("\\s", "");
        System.out.print("Enter source State: ");
        String startState = scanner.nextLine().toLowerCase().replaceAll("\\s", "");
        System.out.print("Enter destination city: ");
        String endCity = scanner.nextLine().toLowerCase().replaceAll("\\s", "");
        System.out.print("Enter destination State: ");
        String endState = scanner.nextLine().toLowerCase().replaceAll("\\s", "");
        System.out.print("Enter Start Time (format: MM/DD/YYYY hh:mm):");
        String startTime = scanner.nextLine();
        scanner.close();
        City origin = new City(startCity, startState);
        City destination = new City(endCity, endState);

        Map<City, City> previousNodes = shortestPathFinder.findShortestPaths(origin);

          if (previousNodes.containsKey(destination)) {
            List<City> shortestPath = shortestPathFinder.getShortestPath(origin, destination, previousNodes);
            double totalGallons = 0.0; // Declare totalGallons before using it
            double shortestDistance = shortestPathFinder.calculatePathDistance(shortestPath);
            City previousCity = new City("","");
            // Print the shortest path with distances between cities and weather conditions
            System.out.print("Shortest path: ");
            LocalDateTime currentDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));
            for (int i = 0; i < shortestPath.size(); i++) {
                City city = shortestPath.get(i);
                
                if (i > 0) {
                    //System.out.print(" -- ");
                    double distance = 0.0;
                    double speed = 0.0;
                    double duration = 0.0;
                    double gallons = 0.0;
                    double gradient = 0.0;
                    double distance_weather=0.0;
                    double distance_gradient =0.0;
                    for (Edge edge : shortestPathFinder.graph.get(shortestPath.get(i - 1))) {
                        if (edge.destination.equals(city)) {
                            distance = edge.distance;
                            gradient = edge.gradient;
                            distance_weather = checkWeatherCondition(distance,shortestPathFinder.getWeatherCondition(city, currentDateTime));
                            distance_gradient = distance_weather * (1+gradient);
                            speed = edge.speed;
                            duration = distance_gradient / speed;
                            gallons = distance_gradient / 38;
                            break;
                        }
                    }
                    currentDateTime = currentDateTime.plusMinutes((long) (duration * 60));
                    totalGallons = totalGallons + gallons;
                    System.out.println("");
                    System.out.print(previousCity);
                    System.out.print("---->");
                    System.out.print(city);
                    System.out.println("\n-------------------");
                    System.out.printf("distance %.2f miles\n", distance);
                    System.out.printf("ETA: %s \n", currentDateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));
                    System.out.printf("Gallons required: %.2f \nWeather: %s\n", gallons, shortestPathFinder.getWeatherCondition(city, currentDateTime));
                    //System.out.print("->");
                }
                previousCity = city;
            }
            System.out.println();
            System.out.printf("Shortest distance from %s to %s: %.2f, total gallons: %.2f\n", origin, destination, shortestDistance, totalGallons);
            System.out.printf("End Time: %s\n", currentDateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));
        } else {
            System.out.println("No path found from " + origin + " to " + destination);
        }
    }

    private String getWeatherCondition(City city, LocalDateTime dateTime) {
        
        int minutes = dateTime.getMinute();
        
        // Check if minutes is less than 30
        if (minutes < 30) {
            // Update minutes to 00
            dateTime = dateTime.withMinute(0);
        } else {
            // Add one hour to the LocalDateTime
            dateTime = dateTime.plusHours(1);
            dateTime = dateTime.withMinute(0);
        }
        WeatherData weatherData = weatherDataMap.get(city);
        if (weatherData != null) {
            return weatherData.hourlyConditions.get(dateTime);
        }
        return "Unknown";
    }

    private static double checkWeatherCondition(double distance, String condition) {
        switch (condition) {
            case "fair":
                distance = distance + 0;
                break;
            case "partlycloudy":
                distance = distance + 1;
                break;
            case "mostlycloudy":
                distance = distance + 2;
                break;
            case "cloudy":
                distance = distance + 3;
                break;
            case "lightrain":
                distance = distance + 4;
                break;
            case "mist":
                distance = distance + 5;
                break;
            case "fog":
                distance = distance + 6;
                break;
            case "lightsnow":
                distance = distance + 7;
                break;
            default:
                distance = distance;
                System.out.println("weather condition unknown.");
        }
        return distance;
    }
}
