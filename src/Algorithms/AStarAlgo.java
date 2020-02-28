package Algorithms;

import utils.GraphNode;
import utils.MapProcessor;
import utils.ShortestPath;

import java.util.*;

public class AStarAlgo {
    /**
     * Heuristic search to find the shortest path
     * @param source - Source node
     * @param destination - Destination node
     * @return ShortestPath object containing path length and List of nodes as path
     * @throws Exception - unable to find path from source to destination
     */
    public static ShortestPath AStarSearch(GraphNode source, GraphNode destination) throws Exception {
        PriorityQueue<AStarNode> toExplore = new PriorityQueue<>(10,
                Comparator.comparingDouble((node) -> node.getEstimatedWeight(destination)));
        HashMap<GraphNode, AStarNode> explored = new HashMap<>();
        AStarNode first = new AStarNode(0, source);
        AStarNode curr;
        toExplore.add(first);
        explored.put(source, first);
        while(!toExplore.isEmpty()){
            curr = toExplore.poll();
            if(curr.getGraphNode() == destination){
                return BackTrack(explored, destination);
            }
            for (Map.Entry<GraphNode, Float> pair: curr.graphNode.getNeighbours()) {
                double updatedWeight = curr.getWeight() + pair.getValue();
                if(!explored.containsKey(pair.getKey())) {
                    AStarNode node = new AStarNode(updatedWeight, pair.getKey());
                    node.setParent(curr);
                    explored.put(pair.getKey(), node);
                    toExplore.add(node);
                }else {
                    AStarNode existing = explored.get(pair.getKey());
                    if (existing.getWeight() > updatedWeight){
                        toExplore.remove(existing);
                        existing.setWeight(updatedWeight);
                        existing.setParent(curr);
                        toExplore.add(existing);
                    }
                }
            }
        }

        throw new Exception();
    }

    /**
     * Backtracks a Hashmap of explored nodes to find the shortest path
     * @param explored - Hashmap of explored nodes
     * @param destination - Destination node
     * @return ShortestPath object containing path length as well as a List of nodes representing the path
     */
    private static ShortestPath BackTrack(HashMap<GraphNode, AStarNode> explored, GraphNode destination) {
        AStarNode curr = explored.get(destination);
        List<GraphNode> result = new LinkedList<>();
        while(curr.getParent() != null){
            result.add(0, curr.graphNode);
            curr = curr.parent;
        }
        result.add(0, curr.graphNode);
        return new ShortestPath(explored.get(destination).weight, result);
    }

    /**
     * Wrapper around a graph node to add extra properties such as parent
     */
    private static class AStarNode {
        private double weight;
        private GraphNode graphNode;
        private AStarNode parent = null;
        public AStarNode(double weight, GraphNode graphNode){
            this.graphNode = graphNode;
            this.weight = weight;
        }
        public double getEstimatedWeight(GraphNode destination){
            return weight + graphNode.getEuclideanDistanceTo(destination) * MapProcessor.FORWARD_WEIGHT;
        }
        public double getWeight(){
            return weight;
        }
        public void setWeight(double weight){
            this.weight = weight;
        }
        public AStarNode getParent(){
            return parent;
        }
        public void setParent(AStarNode parent){
            this.parent = parent;
        }
        public GraphNode getGraphNode(){
            return graphNode;
        }
    }
}
