import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Graph {
    private Map<String, Map<String, Double>> graph;

    public Graph() {
        this.graph = new HashMap<>();
    }

    public void addEdge(String source, String destination, double distance) {
        graph.computeIfAbsent(source, k -> new HashMap<>()).put(destination, distance);
        graph.computeIfAbsent(destination, k -> new HashMap<>()).put(source, distance);
    }

    public Map<String, Double> getNeighbors(String node) {
        return graph.getOrDefault(node, Collections.emptyMap());
    }

    public Set<String> getAllCities() {
        return graph.keySet();
    }

    public void writeToFile() {
        String header = "State_Origin,State_Destination,City_Origin,City_Destination,Distance,Speed,Sea_Level_Diff,Sea_Level_Diff_Miles,Sea_Level_Gradient";
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("MST_Data.csv"))) {
            writer.write(header);
            writer.write("\n");
            for(Map.Entry<String, Map<String, Double>> entry : graph.entrySet()) {
                for(Map.Entry<String, Double> entry1 : entry.getValue().entrySet()) {
                    String content = "State,State," + entry.getKey() + "," + entry1.getKey() + "," + entry1.getValue() + "0,0,0,0";
                    writer.write(content);
                    writer.write("\n");
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}

//Null implies that this is the root of the set
//This is a disjoint set union
class DSU {
    private Map<String, String> parent;

    DSU(Set<String> cities) {
        parent = new HashMap<>();
        for(String city : cities) {
            parent.put(city, null);
        }
    }

    String find(String city) {
        String curr = city;
        while(parent.get(curr) != null) curr = parent.get(curr);
        return curr;
    }

    void unite(String city1, String city2) {
        String parent1 = find(city1);
        String parent2 = find(city2);

        if(parent1 != parent2) {
            parent.put(parent2, parent1);
        }
    }
}



class Node {
    public String val1, val2;
    public Double dis;
    Node(String a, String b, Double dist) {
        val1 = a; val2 = b; dis = dist;
    }
}

class Pair implements Comparable<Pair>{
    public String val1, val2;

    Pair(String a, String b) {
        val1 = a; val2 = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return Objects.equals(val1, pair.val1) &&
                Objects.equals(val2, pair.val2);
    }

    @Override
    public int compareTo(Pair other) {
        int res = this.val1.compareTo(other.val1);
        if(res == 0) return this.val2.compareTo(other.val2);
        return res;
    }
}

public class CityPathFinder {
    private static Map<String, Double> dijkstra(Graph graph, String start, Map<String, String> predecessors) {
        Map<String, Double> distances = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
        Set<String> visited = new HashSet<>();

        distances.put(start, 0.0);
        queue.offer(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            // System.out.println(queue);
            if (visited.contains(current)) {
                System.out.println("TEst");
                continue;
            }

            visited.add(current);
            String nextCity = "";
            for (Map.Entry<String, Double> neighbor : graph.getNeighbors(current).entrySet()) {
                nextCity = neighbor.getKey();

                double newDistance = distances.get(current) + neighbor.getValue();

                System.out.println(current+" -- "+ nextCity+" -- "+neighbor.getValue()+" -- "+distances.get(current) +" -- "+newDistance +" -- "+distances.getOrDefault(nextCity, Double.POSITIVE_INFINITY));
                if (newDistance < distances.getOrDefault(nextCity, Double.POSITIVE_INFINITY)) {
                    distances.put(nextCity, newDistance);
                    predecessors.put(nextCity, current);
                    //queue.offer(nextCity);
                    if (!queue.contains(nextCity))   queue.offer(nextCity);
                    System.out.println(queue);
                    System.out.println(distances);
                }
            }

            // System.out.println(current +" --> "+nextCity);
        }

        return distances;
    }

    private static List<String> reconstructPath(String start, String end, Map<String, String> predecessors) {
        List<String> path = new ArrayList<>();
        String current = end;

        while (current != null) {
            path.add(current);

            current = predecessors.get(current);
        }

        Collections.reverse(path);

        return path;
    }

    static class Node_Comparator implements Comparator<Node> {
        @Override
        public int compare(Node obj1, Node obj2) {
            return Double.compare(obj1.dis, obj2.dis);
        }
    }

    static Graph kruskalAlgo(Graph graph) {
    // Get the edge list
    List<Node> edgeList = new ArrayList<>(); // List of edges
    Set<String> cities = graph.getAllCities(); // Set of cities
    for (String city : cities) { // For every city in cities
        Map<String, Double> neighbors = graph.getNeighbors(city); // Get all neighbors
        for (Map.Entry<String, Double> entry : neighbors.entrySet()) { // Go through neighbors
            edgeList.add(new Node(city, entry.getKey(), entry.getValue())); // Add to edge list
        }
    }

    Collections.sort(edgeList, new Node_Comparator()); // Sort edge list

    // Calculate the minimum spanning tree
    Graph MST = new Graph(); // Minimum Spanning Tree
    DSU set = new DSU(cities);
    double sum = 0;
    int num_edges = 0;

    // Create an adjacency list to store the MST
    Map<String, List<Edge>> adjacencyList = new HashMap<>();

    for (Node edge : edgeList) { // Go through edge list
        if (!set.find(edge.val1).equals(set.find(edge.val2))) { // If the edges don't form a cycle
        //System.out.println("Edge: " + edge.val1 + " -> " + edge.val2 + ", Distance: " + edge.dis); // Print the edge and distance
            set.unite(edge.val1, edge.val2); // Unite them
            sum += edge.dis;
            MST.addEdge(edge.val1, edge.val2, edge.dis); // Then add the edge
            ++num_edges;

            // Build the adjacency list
            Edge forwardEdge = new Edge(edge.val2, edge.dis);
            Edge backwardEdge = new Edge(edge.val1, edge.dis);

            adjacencyList.computeIfAbsent(edge.val1, k -> new ArrayList<>()).add(forwardEdge);
            adjacencyList.computeIfAbsent(edge.val2, k -> new ArrayList<>()).add(backwardEdge);
        }
    }

    // Print the adjacency list
    System.out.println("\nAdjacency List for MST:");
    for (String node : adjacencyList.keySet()) {
        System.out.print(node);
        List<Edge> neighbors = adjacencyList.get(node);
        for (Edge neighbor : neighbors) {
            System.out.print("-->"+neighbor.node + "--> (Distance: " + neighbor.distance + " miles) ");
        }
        System.out.println();
        System.out.println("*****************");
    }

    System.out.println("Num Edges: " + num_edges);
    System.out.println("Total Sum: " + sum+" miles"); // For the dummy set, the total sum should be 19

    return MST;
}

// Define a new Edge class to store both node and distance
static class Edge {
    String node;
    double distance;

    Edge(String node, double distance) {
        this.node = node;
        this.distance = distance;
    }
}


    static Graph primsAlgo(Graph g) {
    Graph MST = new Graph();

    PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(o -> o.dis));

    String startNode = g.getAllCities().iterator().next();
    System.out.println("Start Node: " + startNode);

    // Set to keep track of visited vertices
    Set<String> visited = new HashSet<>();

    // Initial inputs from startNode
    for (Map.Entry<String, Double> neighbor : g.getNeighbors(startNode).entrySet()) {
        queue.add(new Node(startNode, neighbor.getKey(), neighbor.getValue()));
    }

    visited.add(startNode);

    double totalSum = 0.0;
    int num_edges = 0;

    // Create an adjacency list to store the MST
    Map<String, List<Edge>> adjacencyList = new HashMap<>();

    while (!queue.isEmpty() && visited.size() < g.getAllCities().size()) {
        Node newEdge = queue.poll();

        // Ensure that both vertices of the edge are not visited
        if (visited.contains(newEdge.val2)) {
            continue;
        }

        // Add vertices to the visited set
        visited.add(newEdge.val1);
        visited.add(newEdge.val2);

        //System.out.println("Edge: " + newEdge.val1 + " -> " + newEdge.val2 + ", Distance: " + newEdge.dis); // Print the edge and distance

        MST.addEdge(newEdge.val1, newEdge.val2, newEdge.dis);
        ++num_edges;
        totalSum += newEdge.dis;
        totalSum = Double.parseDouble(String.format("%.2f", totalSum));

        // Build the adjacency list
        Edge forwardEdge = new Edge(newEdge.val2, newEdge.dis);
        Edge backwardEdge = new Edge(newEdge.val1, newEdge.dis);

        adjacencyList.computeIfAbsent(newEdge.val1, k -> new ArrayList<>()).add(forwardEdge);
        adjacencyList.computeIfAbsent(newEdge.val2, k -> new ArrayList<>()).add(backwardEdge);

        // Add neighbors of the newly added vertex to the queue
        for (Map.Entry<String, Double> neighbor : g.getNeighbors(newEdge.val2).entrySet()) {
            String nextCity = neighbor.getKey();
            Double dis = neighbor.getValue();

            queue.add(new Node(newEdge.val2, nextCity, dis));
        }
    }

    // Print the adjacency list
    System.out.println("\nAdjacency List for MST:");
    for (String node : adjacencyList.keySet()) {
        System.out.print(node );
        List<Edge> neighbors = adjacencyList.get(node);
        for (Edge neighbor : neighbors) {
            System.out.print("--->"+neighbor.node + " (Distance: " + neighbor.distance + ") miles ");
        }
        System.out.println();
        System.out.println("*****************");
    }

    System.out.println("Num Edges: " + num_edges);
    System.out.println("Total Sum: " + totalSum + " miles");

    return MST;
}

    public static void main(String[] args) {
        Graph graph = new Graph();

        // Read data from CSV file
        String csvFile = "C:\\Users\\PNW_checkout\\Documents\\Algo\\project\\Milestone3\\Final_Connection_V3.csv";
        String line;
        String csvSplitBy = ",";
        boolean firstLine = true;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    // Skip the first line
                    firstLine = false;
                    continue;
                }
                // use tab as separator
                String[] data = line.split(csvSplitBy);
                if(data.length == 0) break;
                String source = data[2].toLowerCase().replaceAll("\\s", "");
                String destination = data[3].toLowerCase().replaceAll("\\s", "");
                double distance = Double.parseDouble(data[4].trim());

                graph.addEdge(source, destination, distance);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the Algorithm to be run:\n1. Prims\n2. Kruskal's");
        int choice = Integer.parseInt(scanner.nextLine());
        if (choice ==1){
        System.out.println("Prims:");
            Graph MST = primsAlgo(graph);
            MST.writeToFile();
        }else if (choice==2){
            System.out.println("Kruskals:");
            Graph MST = kruskalAlgo(graph);
            MST.writeToFile();
        }
        else{
            System.out.println("Choose either Prims or Kruskals Algorithms");
        }
    }
}
