import Algorithms.AStarAlgo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Graph;
import utils.GraphNode;
import utils.Map;
import utils.ShortestPath;


public class GraphTest {
    @Test
    public void ThreeByThreeMiddleBlockedLoop() throws Exception {
        GraphNode g1 = new GraphNode(1, 1, false);
        GraphNode g2 = new GraphNode(1, 2, false);
        GraphNode g3 = new GraphNode(1, 3, false);
        GraphNode g4 = new GraphNode(2, 1, false);
        GraphNode g6 = new GraphNode(2, 3, false);
        GraphNode g7 = new GraphNode(3, 1, false);
        GraphNode g8 = new GraphNode(3, 2, false);
        GraphNode g9 = new GraphNode(3, 3, false);
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

        Graph graph = new Graph(g4, g8, g7, g6);
        ShortestPath result = graph.GetShortestPath();
        Assertions.assertEquals(6, result.getWeight());
        Assertions.assertEquals(7, result.getPath().size());
        Assertions.assertEquals(g4, result.getPath().get(0));
        Assertions.assertEquals(g1, result.getPath().get(1));
        Assertions.assertEquals(g2, result.getPath().get(2));
        Assertions.assertEquals(g3, result.getPath().get(3));
        Assertions.assertEquals(g6, result.getPath().get(4));
        Assertions.assertEquals(g9, result.getPath().get(5));
        Assertions.assertEquals(g8, result.getPath().get(6));
    }

    @Test
    public void ThreeByThree() throws Exception {
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

        g4.addNeighbour(g5, 2f);
        g5.addNeighbour(g4, 2f);
        g5.addNeighbour(g6, 1f);
        g6.addNeighbour(g5, 1f);

        g7.addNeighbour(g8, 1f);
        g8.addNeighbour(g7, 1f);
        g8.addNeighbour(g9, 1f);
        g9.addNeighbour(g8, 1f);

        g1.addNeighbour(g4, 1f);
        g4.addNeighbour(g1, 1f);
        g4.addNeighbour(g7, 2f);
        g7.addNeighbour(g4, 2f);

        g2.addNeighbour(g5, 1f);
        g5.addNeighbour(g2, 1f);
        g5.addNeighbour(g8, 1f);
        g8.addNeighbour(g5, 1f);

        g3.addNeighbour(g6, 1f);
        g6.addNeighbour(g3, 1f);
        g6.addNeighbour(g9, 1f);
        g9.addNeighbour(g6, 1f);
        Graph graph = new Graph(g4, g6, g2, g8);
        ShortestPath result = graph.GetShortestPath();
        Assertions.assertEquals(4, result.getWeight());
        Assertions.assertEquals(5, result.getPath().size());
        Assertions.assertEquals(g4, result.getPath().get(0));
        Assertions.assertEquals(g1, result.getPath().get(1));
        Assertions.assertEquals(g2, result.getPath().get(2));
        Assertions.assertEquals(g3, result.getPath().get(3));
        Assertions.assertEquals(g6, result.getPath().get(4));
    }

    @Test
    public void MapTest(){
        // Sample map on NTU Learn
        Map map = new Map();
        //(4,5) to (9,5) obstacle
        for(int i = 3; i <= 10; i++) {
            map.getCell(i, 6).setVirtualWall(true);
            map.getCell(i, 5).setObstacleStatus(true);
            map.getCell(i, 4).setVirtualWall(true);
        }

        //(14,4) to (14,7) obstacle
        for(int j = 3; j <= 8; j++){
            map.getCell(13, j).setVirtualWall(true);
            map.getCell(14, j).setObstacleStatus(true);
        }
        map.getCell(12, 3).setVirtualWall(true);
        map.getCell(12, 4).setVirtualWall(true);
        map.getCell(12, 5).setVirtualWall(true);
        map.getCell(13, 4).setObstacleStatus(true);
        map.getCell(13, 4).setVirtualWall(false);

        //(0,10) to (3,10) obstacle
        for(int i = 0; i <= 4; i++) {
            map.getCell(i, 11).setVirtualWall(true);
            map.getCell(i, 10).setObstacleStatus(true);
            map.getCell(i, 9).setVirtualWall(true);
        }
        map.getCell(2, 12).setVirtualWall(true);
        map.getCell(3, 12).setVirtualWall(true);
        map.getCell(4, 12).setVirtualWall(true);
        map.getCell(3, 11).setObstacleStatus(true);
        map.getCell(3, 11).setVirtualWall(false);

        //(0,15) to (3,15) obstacle
        for(int i = 0; i <= 4; i++) {
            map.getCell(i, 16).setVirtualWall(true);
            map.getCell(i, 15).setObstacleStatus(true);
            map.getCell(i, 14).setVirtualWall(true);
        }

        //(7,14) to (9,14) obstacle
        for(int i = 6; i <= 10; i++) {
            map.getCell(i, 13).setVirtualWall(true);
            map.getCell(i, 14).setObstacleStatus(true);
            map.getCell(i, 15).setVirtualWall(true);
        }
        map.getCell(6, 16).setVirtualWall(true);
        map.getCell(7, 16).setVirtualWall(true);
        map.getCell(8, 16).setVirtualWall(true);
        map.getCell(7, 15).setObstacleStatus(true);
        map.getCell(7, 15).setVirtualWall(false);
        map.getCell(8, 11).setVirtualWall(true);
        map.getCell(9, 11).setVirtualWall(true);
        map.getCell(10, 11).setVirtualWall(true);
        map.getCell(9, 12).setObstacleStatus(true);
        map.getCell(9, 12).setVirtualWall(false);
        map.getCell(9, 13).setObstacleStatus(true);
        map.getCell(9, 13).setVirtualWall(false);

        //(14,4) to (14,7) obstacle
        for(int j = 12; j <= 15; j++){
            map.getCell(13, j).setVirtualWall(true);
            map.getCell(14, j).setObstacleStatus(true);
        }

        //(7,19) obstacle
        for(int i = 6; i <= 8; i++) {
            map.getCell(i, 18).setVirtualWall(true);
            map.getCell(i, 19).setObstacleStatus(true);
        }

        Graph graph = new Graph(map, 5, 14);
        ShortestPath result = graph.GetShortestPath();
        System.out.println(result.getWeight());
        for(GraphNode n: result.getPath()){
            System.out.println("Coordinate: (" + n.getX() + ", " + n.getY() + "), Orientation: " + (n.isHorizontal()? "horizontal":"vertical"));
        }

    }
}
