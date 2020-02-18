package utils;

import java.util.List;

/**
 * Class to represent shortest path result
 */
public class ShortestPath{
    private double weight;
    private List<GraphNode> path;

    public ShortestPath(double weight, List<GraphNode> path) {
        this.weight = weight;
        this.path = path;
    }

    public List<GraphNode> getPath(){
        return path;
    }

    public double getWeight(){
        return weight;
    }
}