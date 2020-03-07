import algorithms.AStarAlgo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import path.GraphNode;
import path.ShortestPath;


public class AStarTest {
    @Test
    public void ZeroGraph() {
        GraphNode g = new GraphNode(1,2,false);
        ShortestPath result = AStarAlgo.AStarSearch(g, g);
        assert result != null;
        Assertions.assertEquals(0, result.getWeight());
        Assertions.assertEquals(1, result.getPath().size());
        Assertions.assertEquals(g, result.getPath().get(0));
    }

    @Test
    public void TwoNodeGraph() {
        GraphNode g1 = new GraphNode(1,2,false);
        GraphNode g2 = new GraphNode(2,2,false);
        g1.addNeighbour(g2, 1f);
        g2.addNeighbour(g1, 1f);
        ShortestPath result = AStarAlgo.AStarSearch(g1, g2);
        assert result != null;
        Assertions.assertEquals(1, result.getWeight());
        Assertions.assertEquals(2, result.getPath().size());
        Assertions.assertEquals(g1, result.getPath().get(0));
        Assertions.assertEquals(g2, result.getPath().get(1));
    }


    @Test
    public void ThreeNodeGraph() {
        GraphNode g1 = new GraphNode(1,2,false);
        GraphNode g2 = new GraphNode(2,2,false);
        GraphNode g3 = new GraphNode(2,1,false);
        GraphNode g4 = new GraphNode(1,1,false);
        g1.addNeighbour(g2, 1f);
        g1.addNeighbour(g4, 1f);
        g2.addNeighbour(g1, 1f);
        g2.addNeighbour(g3, 1f);
        g3.addNeighbour(g2, 1f);
        g3.addNeighbour(g4, 2f);
        g4.addNeighbour(g1, 1f);
        g4.addNeighbour(g3, 2f);
        ShortestPath result = AStarAlgo.AStarSearch(g1, g3);
        assert result != null;
        Assertions.assertEquals(2, result.getWeight());
        Assertions.assertEquals(3, result.getPath().size());
        Assertions.assertEquals(g1, result.getPath().get(0));
        Assertions.assertEquals(g2, result.getPath().get(1));
        Assertions.assertEquals(g3, result.getPath().get(2));
    }

    @Test
    public void ThreeNodeGraph2() {
        GraphNode g1 = new GraphNode(1,2,false);
        GraphNode g2 = new GraphNode(2,2,false);
        GraphNode g3 = new GraphNode(2,1,false);
        GraphNode g4 = new GraphNode(1,1,false);
        g1.addNeighbour(g2, 1f);
        g1.addNeighbour(g4, 1f);
        g2.addNeighbour(g1, 1f);
        g2.addNeighbour(g3, 2f);
        g3.addNeighbour(g2, 2f);
        g3.addNeighbour(g4, 1f);
        g4.addNeighbour(g1, 1f);
        g4.addNeighbour(g3, 1f);
        ShortestPath result = AStarAlgo.AStarSearch(g1, g3);
        assert result != null;
        Assertions.assertEquals(2, result.getWeight());
        Assertions.assertEquals(3, result.getPath().size());
        Assertions.assertEquals(g1, result.getPath().get(0));
        Assertions.assertEquals(g4, result.getPath().get(1));
        Assertions.assertEquals(g3, result.getPath().get(2));
    }

    @Test
    public void ThreeByThree() {
        GraphNode g1 = new GraphNode(1,1,false);
        GraphNode g2 = new GraphNode(1,2,false);
        GraphNode g3 = new GraphNode(1,3,false);
        GraphNode g4 = new GraphNode(2,1,false);
        GraphNode g5 = new GraphNode(2,2,false);
        GraphNode g6 = new GraphNode(2,3,false);
        GraphNode g7 = new GraphNode(3,1,false);
        GraphNode g8 = new GraphNode(3,2,false);
        GraphNode g9 = new GraphNode(3,3, false);
        g1.addNeighbour(g2, 1f);
        g2.addNeighbour(g1, 1f);
        g2.addNeighbour(g3, 1f);
        g3.addNeighbour(g2, 1f);

        g4.addNeighbour(g5, 1f);
        g5.addNeighbour(g4, 1f);
        g5.addNeighbour(g6, 1f);
        g6.addNeighbour(g5, 1f);

        g7.addNeighbour(g8, 1f);
        g8.addNeighbour(g7, 1f);
        g8.addNeighbour(g9, 1f);
        g9.addNeighbour(g8, 1f);

        g1.addNeighbour(g4, 1f);
        g4.addNeighbour(g1, 1f);
        g4.addNeighbour(g7, 1f);
        g7.addNeighbour(g4, 1f);

        g2.addNeighbour(g5, 1f);
        g5.addNeighbour(g2, 1f);
        g5.addNeighbour(g8, 1f);
        g8.addNeighbour(g5, 1f);

        g3.addNeighbour(g6, 1f);
        g6.addNeighbour(g3, 1f);
        g6.addNeighbour(g9, 1f);
        g9.addNeighbour(g6, 1f);
        ShortestPath result = AStarAlgo.AStarSearch(g4, g6);
        assert result != null;
        Assertions.assertEquals(2, result.getWeight());
        Assertions.assertEquals(3, result.getPath().size());
        Assertions.assertEquals(g4, result.getPath().get(0));
        Assertions.assertEquals(g5, result.getPath().get(1));
        Assertions.assertEquals(g6, result.getPath().get(2));
    }

    @Test
    public void ThreeByThree2() {
        GraphNode g1 = new GraphNode(1,1,false);
        GraphNode g3 = new GraphNode(1,3,false);
        GraphNode g4 = new GraphNode(2,1,false);
        GraphNode g5 = new GraphNode(2,2,false);
        GraphNode g6 = new GraphNode(2,3,false);
        GraphNode g7 = new GraphNode(3,1,false);
        GraphNode g8 = new GraphNode(3,2,false);
        GraphNode g9 = new GraphNode(3,3, false);

        g4.addNeighbour(g5, 1f);
        g5.addNeighbour(g4, 1f);
        g5.addNeighbour(g6, 1f);
        g6.addNeighbour(g5, 1f);

        g7.addNeighbour(g8, 1f);
        g8.addNeighbour(g7, 1f);
        g8.addNeighbour(g9, 1f);
        g9.addNeighbour(g8, 1f);

        g1.addNeighbour(g4, 1f);
        g4.addNeighbour(g1, 1f);
        g4.addNeighbour(g7, 1f);
        g7.addNeighbour(g4, 1f);

        g5.addNeighbour(g8, 1f);
        g8.addNeighbour(g5, 1f);

        g3.addNeighbour(g6, 1f);
        g6.addNeighbour(g3, 1f);
        g6.addNeighbour(g9, 1f);
        g9.addNeighbour(g6, 1f);
        ShortestPath result = AStarAlgo.AStarSearch(g4, g6);
        assert result != null;
        Assertions.assertEquals(2, result.getWeight());
        Assertions.assertEquals(3, result.getPath().size());
        Assertions.assertEquals(g4, result.getPath().get(0));
        Assertions.assertEquals(g5, result.getPath().get(1));
        Assertions.assertEquals(g6, result.getPath().get(2));
    }

    @Test
    public void ThreeByThree3() {
        GraphNode g1 = new GraphNode(1,1,false);
        GraphNode g3 = new GraphNode(1,3,false);
        GraphNode g4 = new GraphNode(2,1,false);
        GraphNode g5 = new GraphNode(2,2,false);
        GraphNode g6 = new GraphNode(2,3,false);
        GraphNode g8 = new GraphNode(3,2,false);
        GraphNode g9 = new GraphNode(3,3, false);

        g4.addNeighbour(g5, 1f);
        g5.addNeighbour(g4, 1f);
        g5.addNeighbour(g6, 1f);
        g6.addNeighbour(g5, 1f);

        g8.addNeighbour(g9, 1f);
        g9.addNeighbour(g8, 1f);

        g1.addNeighbour(g4, 1f);
        g4.addNeighbour(g1, 1f);

        g5.addNeighbour(g8, 1f);
        g8.addNeighbour(g5, 1f);

        g3.addNeighbour(g6, 1f);
        g6.addNeighbour(g3, 1f);
        g6.addNeighbour(g9, 1f);
        g9.addNeighbour(g6, 1f);
        ShortestPath result = AStarAlgo.AStarSearch(g4, g6);
        assert result != null;
        Assertions.assertEquals(2, result.getWeight());
        Assertions.assertEquals(3, result.getPath().size());
        Assertions.assertEquals(g4, result.getPath().get(0));
        Assertions.assertEquals(g5, result.getPath().get(1));
        Assertions.assertEquals(g6, result.getPath().get(2));
    }

    @Test
    public void ThreeByThreeMiddleBlocked() {
        GraphNode g1 = new GraphNode(1,1,false);
        GraphNode g2 = new GraphNode(1,2,false);
        GraphNode g3 = new GraphNode(1,3,false);
        GraphNode g4 = new GraphNode(2,1,false);
        GraphNode g6 = new GraphNode(2,3,false);
        GraphNode g7 = new GraphNode(3,1,false);
        GraphNode g8 = new GraphNode(3,2,false);
        GraphNode g9 = new GraphNode(3,3, false);
        g1.addNeighbour(g2, 1f);
        g2.addNeighbour(g1, 1f);
        g2.addNeighbour(g3, 1f);
        g3.addNeighbour(g2, 1f);


        g7.addNeighbour(g8, 1f);
        g8.addNeighbour(g7, 1f);
        g8.addNeighbour(g9, 1f);
        g9.addNeighbour(g8, 1f);

        g1.addNeighbour(g4, 1f);
        g4.addNeighbour(g1, 1f);
        g4.addNeighbour(g7, 2f);
        g7.addNeighbour(g4, 2f);


        g3.addNeighbour(g6, 1f);
        g6.addNeighbour(g3, 1f);
        g6.addNeighbour(g9, 1f);
        g9.addNeighbour(g6, 1f);
        ShortestPath result = AStarAlgo.AStarSearch(g4, g6);
        assert result != null;
        Assertions.assertEquals(4, result.getWeight());
        Assertions.assertEquals(5, result.getPath().size());
        Assertions.assertEquals(g4, result.getPath().get(0));
        Assertions.assertEquals(g1, result.getPath().get(1));
        Assertions.assertEquals(g2, result.getPath().get(2));
        Assertions.assertEquals(g3, result.getPath().get(3));
        Assertions.assertEquals(g6, result.getPath().get(4));
    }


    @Test
    public void ThreeByThreeMiddleBlockedLoop() {
        GraphNode g1 = new GraphNode(1,1,false);
        GraphNode g2 = new GraphNode(1,2,false);
        GraphNode g3 = new GraphNode(1,3,false);
        GraphNode g4 = new GraphNode(2,1,false);
        GraphNode g6 = new GraphNode(2,3,false);
        GraphNode g7 = new GraphNode(3,1,false);
        GraphNode g8 = new GraphNode(3,2,false);
        GraphNode g9 = new GraphNode(3,3, false);
        g1.addNeighbour(g2, 1f);
        g2.addNeighbour(g1, 1f);
        g2.addNeighbour(g3, 1f);
        g3.addNeighbour(g2, 1f);


        g7.addNeighbour(g8, 1f);
        g8.addNeighbour(g7, 1f);
        g8.addNeighbour(g9, 1f);
        g9.addNeighbour(g8, 1f);

        g1.addNeighbour(g4, 1f);
        g4.addNeighbour(g1, 1f);
        g4.addNeighbour(g7, 8f);
        g7.addNeighbour(g4, 8f);


        g3.addNeighbour(g6, 1f);
        g6.addNeighbour(g3, 1f);
        g6.addNeighbour(g9, 1f);
        g9.addNeighbour(g6, 1f);
        ShortestPath result = AStarAlgo.AStarSearch(g4, g7);
        assert result != null;
        Assertions.assertEquals(7, result.getWeight());
        Assertions.assertEquals(8, result.getPath().size());
        Assertions.assertEquals(g4, result.getPath().get(0));
        Assertions.assertEquals(g1, result.getPath().get(1));
        Assertions.assertEquals(g2, result.getPath().get(2));
        Assertions.assertEquals(g3, result.getPath().get(3));
        Assertions.assertEquals(g6, result.getPath().get(4));
        Assertions.assertEquals(g9, result.getPath().get(5));
        Assertions.assertEquals(g8, result.getPath().get(6));
        Assertions.assertEquals(g7, result.getPath().get(7));
    }
}
