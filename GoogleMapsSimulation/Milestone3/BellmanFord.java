import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class City {
    String name;
    String state;
    double latitude;
    double longitude;

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
    double latitude;
    double longitude;

    public WeatherData(double latitude, double longitude) {
        this.hourlyConditions = new HashMap<>();
        this.latitude = latitude;
        this.longitude = longitude;
    }

}

public class BellmanFord {
    public static void main(String[] args) {
        BellmanFord bellmanFord = new BellmanFord();
        bellmanFord.readDataFromCSV("Final_Connection_V3.csv");
        bellmanFord.readWeatherData("collected Weather.csv");

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
        System.out.println("Origin: " + origin);
        City destination = new City(endCity, endState);

        Map<City, City> previousNodes = bellmanFord.findShortestPaths(origin, startTime);

        if (previousNodes.containsKey(destination)) {
            List<City> shortestPath = bellmanFord.getShortestPath(origin, destination, previousNodes);
            double totalGallons = 0.0;

            double shortestDistance = bellmanFord.calculatePathDistance(shortestPath);
            City previousCity = new City("", "");

            LocalDateTime currentDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"));

            
            WeatherData startWeather = bellmanFord.weatherDataMap.get(origin);
            if (startWeather != null) {
                System.out.printf("Starting Latitude: %.2f, Longitude: %.2f\n", startWeather.latitude, startWeather.longitude);
            }

            
            System.out.print(startCity);
            if (!bellmanFord.visitedCities.contains(origin)) {
                double distance = 0.0;
                double gallons = 0.0;
                String weatherCondition = bellmanFord.getWeatherCondition(origin, currentDateTime);

            
                List<String> attributes = new ArrayList<>();
                attributes.add(origin.name);
                attributes.add(origin.state);
                attributes.add(String.valueOf(distance));
                attributes.add(String.valueOf(gallons));
                attributes.add(weatherCondition);
                attributes.add(String.valueOf(startWeather.latitude));  
                attributes.add(String.valueOf(startWeather.longitude));
                BellmanFord.cityAttributes.add(attributes);

                
                currentDateTime = currentDateTime.plusMinutes(0); 

                System.out.printf("--distance %.2f miles", distance);
                System.out.printf("--ETA: %s ", currentDateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));
                System.out.printf("--Gallons required: %.2f -- Weather: %s", gallons, weatherCondition);

                bellmanFord.visitedCities.add(origin);
            }

            
            for (int i = 0; i < shortestPath.size(); i++) {
                City city = shortestPath.get(i);

                if (i > 0) {
                    double distance = 0.0;
                    double speed = 0.0;
                    double duration = 0.0;
                    double gallons = 0.0;
                    double gradient = 0.0;
                    double distanceWeather = 0.0;
                    double distanceGradient = 0.0;

                    for (Edge edge : bellmanFord.graph.get(shortestPath.get(i - 1))) {
                        if (edge.destination.equals(city)) {
                            distance = edge.distance;
                            gradient = edge.gradient;
                            WeatherData cityWeather = bellmanFord.weatherDataMap.get(city);
                            if (cityWeather != null) {
                                distanceWeather = bellmanFord.checkWeatherCondition(distance, bellmanFord.getWeatherCondition(city, currentDateTime));
                                distanceGradient = distanceWeather * (1 + gradient);
                                speed = edge.speed;
                                duration = distanceGradient / speed;
                                gallons = distanceGradient / 38;
                                String formattedGallons = String.format("%.2f", gallons);
                                List<String> attributes = new ArrayList<>();
                                attributes.add(city.name);
                                attributes.add(city.state);
                                attributes.add(String.valueOf(distance));
                                attributes.add(String.valueOf(formattedGallons));
                                attributes.add(bellmanFord.getWeatherCondition(city, currentDateTime));
                                attributes.add(String.valueOf(cityWeather.latitude));  
                                attributes.add(String.valueOf(cityWeather.longitude));
                                BellmanFord.cityAttributes.add(attributes);
                                currentDateTime = currentDateTime.plusMinutes((long) (duration * 60));
                                totalGallons += gallons;
                                if (!bellmanFord.visitedCities.contains(city)) {
                                    System.out.print("---->");
                                    System.out.print(city);
                                    System.out.printf("--distance %.2f miles", distance);
                                    System.out.printf("--ETA: %s ", currentDateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));
                                    System.out.printf("--Gallons required: %s -- Weather: %s", formattedGallons, bellmanFord.getWeatherCondition(city, currentDateTime));
                                    bellmanFord.visitedCities.add(city);
                                }

                                if (city.equals(destination)) {
                                    break;  
                                }
                            }
                        }
                    }
                }
                previousCity = city;
            }

            System.out.println();
            System.out.println("\nCity Attributes for the Shortest Path:");
            System.out.printf("%-20s%-20s%-20s%-20s%-20s%-20s%-20s\n", "City", "State", "Distance", "Gallons", "Weather", "Latitude", "Longitude");
            for (List<String> attributes : BellmanFord.cityAttributes) {
                System.out.printf("%-20s%-20s%-20s%-20s%-20s%-20s%-20s\n", attributes.get(0), attributes.get(1), attributes.get(2), attributes.get(3), attributes.get(4), attributes.get(5), attributes.get(6));
            }
            BellmanFord.writeCityAttributesToCSV(BellmanFord.cityAttributes, "city_attributes.csv");

            System.out.printf("\nShortest distance from %s to %s: %.2f, total gallons: %.2f\n", origin, destination, shortestDistance, totalGallons);
            System.out.printf("End Time: %s\n", currentDateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")));
        } else {
            System.out.println("No path found from " + origin + " to " + destination);
        }
    }
    
    private Map<City, List<Edge>> graph;
    private static Map<City, WeatherData> weatherDataMap;
    public static List<List<String>> cityAttributes;
    private Map<City, Double> distances;
    private Map<City, City> previousNodes;
    private Set<City> visitedCities;  

    public BellmanFord() {
        this.graph = new LinkedHashMap<>();
        this.weatherDataMap = new HashMap<>();
        this.distances = new HashMap<>();
        this.previousNodes = new HashMap<>();
        this.cityAttributes = new ArrayList<>();
        this.visitedCities = new HashSet<>();  
    }

    public void addEdge(City source, City destination, double distance, double speed, double gradient) {
        if (!source.state.equals(destination.state)) {
            source.state = source.state.isEmpty() ? destination.state : source.state;
            destination.state = destination.state.isEmpty() ? source.state : destination.state;
        }

        List<Edge> sourceEdges = graph.computeIfAbsent(source, k -> new ArrayList<>());
        List<Edge> destinationEdges = graph.computeIfAbsent(destination, k -> new ArrayList<>());
        if (sourceEdges.stream().noneMatch(edge -> edge.destination.equals(destination))
                && destinationEdges.stream().noneMatch(edge -> edge.destination.equals(source))) {
            sourceEdges.add(new Edge(source, destination, distance, speed, gradient));
            destinationEdges.add(new Edge(destination, source, distance, speed, gradient));
        }
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
            e.printStackTrace(); 
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

                double latitude = Double.parseDouble(parts[parts.length - 2].trim());
                double longitude = Double.parseDouble(parts[parts.length - 1].trim());

                WeatherData weatherData = new WeatherData(latitude, longitude);
                for (int i = 2; i < parts.length - 2; i++) {
                    String dateTimeString = (firstLineData[i]).trim();

                    if (!dateTimeString.equalsIgnoreCase("Latitude")) {  
                        String conditions = parts[i].trim().toLowerCase().replaceAll("\\s", "");
                        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("MM/dd/yyyy H:mm", Locale.ENGLISH));

                        weatherData.hourlyConditions.put(dateTime, conditions);
                    }
                }
                weatherDataMap.put(new City(cityName, stateName), weatherData);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace(); 
        }
    }

    public Map<City, City> findShortestPaths(City start, String startTime) {
        // Initialize distances
        for (City city : graph.keySet()) {
            distances.put(city, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);

        // Relax edges repeatedly
        for (int i = 0; i < graph.size() - 1; i++) {
            for (City current : graph.keySet()) {
                for (Edge edge : graph.getOrDefault(current, Collections.emptyList())) {
                    City neighbor = edge.destination;
                    double newDistance = distances.get(current) + edge.distance;

                    if (newDistance < distances.get(neighbor)) {
                        distances.put(neighbor, newDistance);
                        previousNodes.put(neighbor, current);
                    }
                }
            }
        }
        for (City current : graph.keySet()) {
            for (Edge edge : graph.getOrDefault(current, Collections.emptyList())) {
                City neighbor = edge.destination;
                double newDistance = distances.get(current) + edge.distance;

                if (newDistance < distances.get(neighbor)) {
                    throw new RuntimeException("Graph contains a negative-weight cycle");
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
            }
        }

        return distance;
    }

    public static void writeCityAttributesToCSV(List<List<String>> cityAttributes, String csvFilePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            writer.write("City,State,Distance,Gallons,Weather,Latitude,Longitude\n");
            for (List<String> attributes : cityAttributes) {
                String row = String.join(",", attributes) + "\n";
                writer.write(row);
            }
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

   

    private String getWeatherCondition(City city, LocalDateTime dateTime) {
        WeatherData weatherData = weatherDataMap.get(city);

        if (weatherData != null) {
            LocalDateTime roundedDateTime = roundDownToNearestHour(dateTime);
            if (weatherData.hourlyConditions.containsKey(roundedDateTime)) {
                return weatherData.hourlyConditions.get(roundedDateTime);
            } else {
                return "Unknown";
            }
        }

        return "Unknown";
    }

    private LocalDateTime roundDownToNearestHour(LocalDateTime dateTime) {
        return dateTime.withMinute(0).withSecond(0).withNano(0);
    }

    private double checkWeatherCondition(double distance, String condition) {
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
            case "heavyrain":
                distance = distance + 5;
                break;
        }
        return distance;
    }
}
