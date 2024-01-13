import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

public class CityFinder {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Read CSV file and store data in a list
        List<CityDistance> cityDistances = readCSV("Final_Dataset_Algo.csv");

        // Input source and destination from the user
        System.out.print("Enter source city: ");
        String startCity = scanner.nextLine().toLowerCase().replaceAll("\\s", "");
        System.out.print("Enter source State: ");
        String startState = scanner.nextLine().toLowerCase().replaceAll("\\s", "");
        System.out.print("Enter destination city: ");
        String endCity = scanner.nextLine().toLowerCase().replaceAll("\\s", "");
        System.out.print("Enter destination State: ");
        String endState = scanner.nextLine().toLowerCase().replaceAll("\\s", "");
        scanner.close();
        endCity = endCity + endState;

        // Create a queue to store the cities
        Queue<String> cityQueue = new LinkedList<>();
        startCity = startCity + startState;
        cityQueue.add(startCity);
        System.out.print(startCity);

        // Create a set to keep track of visited cities
        Set<String> visitedCities = new HashSet<>();
        double final_distance = 0;

        // Continue until the queue is not empty
        while (!cityQueue.isEmpty()) {
            String currentCity = cityQueue.poll();
            visitedCities.add(currentCity);

            CityDistance nearestCity = findNearestCity(cityDistances, currentCity, visitedCities);

            if (nearestCity != null) {
                System.out.print("--->" + nearestCity.getDistance() + "--->" + nearestCity.getCity());
                final_distance = final_distance + nearestCity.getDistance();
                if (!nearestCity.getCity().equals(endCity)) {
                    cityQueue.add(nearestCity.getCity());
                } else {
                    System.out.println("\nDestination reached!, total distance :" + Math.round(final_distance * 100.0) / 100.0);
                    break;
                }
            } else {
                System.out.println("\nNo more cities to explore!");
                break;
            }
        }
    }

    private static List<CityDistance> readCSV(String filePath) {
        List<CityDistance> cityDistances = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Read the header line
            String header = br.readLine();
            String[] columnNames = header.split(",");

            // Check if columnNames is not empty and has at least 3 elements
            if (columnNames.length < 3) {
                System.out.println("Invalid CSV file format. Ensure that it has at least 3 columns.");
                return cityDistances;
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                // Make sure the array has at least 3 elements
                if (values.length >= 3) {
                    String cityOrigin = values[0].toLowerCase().replaceAll("\\s", "");
                    String cityDestination = values[1].toLowerCase().replaceAll("\\s", "");

                    // Make sure the distance is a valid double
                    double distance;
                    try {
                        distance = Double.parseDouble(values[2].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid distance value in the CSV file: " + values[2].trim());
                        continue;
                    }

                    cityDistances.add(new CityDistance(cityOrigin, cityDestination, distance));
                } else {
                    System.out.println("Invalid CSV line: " + Arrays.toString(values));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cityDistances;
    }

    private static CityDistance findNearestCity(List<CityDistance> cityDistances, String source,
            Set<String> visitedCities) {
        CityDistance nearestCity = null;
        double minDistance = Double.MAX_VALUE;

        for (CityDistance cityDistance : cityDistances) {
            if (cityDistance.getCityOrigin().equals(source)
                    && !visitedCities.contains(cityDistance.getCityDestination())
                    && cityDistance.getDistance() < minDistance) {
                minDistance = cityDistance.getDistance();
                nearestCity = cityDistance;
            }
        }

        return nearestCity;
    }

    

    private static class CityDistance {
        private String cityOrigin;
        private String cityDestination;
        private double distance; // Change data type to double

        public CityDistance(String cityOrigin, String cityDestination, double distance) {
            this.cityOrigin = cityOrigin;
            this.cityDestination = cityDestination;
            this.distance = distance;
        }

        public String getCityOrigin() {
            return cityOrigin;
        }

        public String getCityDestination() {
            return cityDestination;
        }

        public double getDistance() {
            return distance;
        }

        public String getCity() {
            return cityDestination;
        }
    }
}
