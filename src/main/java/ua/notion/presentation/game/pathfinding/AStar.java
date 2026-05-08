package ua.notion.presentation.game.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import javafx.geometry.Point2D;

public class AStar {
  private static class Node {
    int x, y;
    double g, h;
    Node parent;

    Node(int x, int y) {
      this.x = x;
      this.y = y;
    }

    double f() {
      return g + h;
    }
  }

  public static List<Point2D> findPath(
      int startX, int startY, int endX, int endY, int mapWidth, int mapHeight) {
    PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::f));
    Set<String> closedSet = new HashSet<>();
    Map<String, Node> allNodes = new HashMap<>();

    Node start = new Node(startX, startY);
    start.g = 0;
    start.h = heuristic(startX, startY, endX, endY);

    openSet.add(start);
    allNodes.put(key(startX, startY), start);

    while (!openSet.isEmpty()) {
      Node current = openSet.poll();

      if (current.x == endX && current.y == endY) {
        return reconstructPath(current);
      }

      closedSet.add(key(current.x, current.y));

      for (int[] dir : new int[][] {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}) {
        int nx = current.x + dir[0];
        int ny = current.y + dir[1];

        if (nx < 0 || nx >= mapWidth || ny < 0 || ny >= mapHeight) continue;
        if (closedSet.contains(key(nx, ny))) continue;

        double tentativeG = current.g + 1;

        Node neighbor = allNodes.get(key(nx, ny));
        if (neighbor == null) {
          neighbor = new Node(nx, ny);
          neighbor.h = heuristic(nx, ny, endX, endY);
          allNodes.put(key(nx, ny), neighbor);
        }

        if (tentativeG < neighbor.g || neighbor.g == 0) {
          neighbor.parent = current;
          neighbor.g = tentativeG;

          if (!openSet.contains(neighbor)) {
            openSet.add(neighbor);
          }
        }
      }
    }

    return Collections.emptyList();
  }

  private static double heuristic(int x1, int y1, int x2, int y2) {
    return Math.abs(x1 - x2) + Math.abs(y1 - y2);
  }

  private static String key(int x, int y) {
    return x + "," + y;
  }

  private static List<Point2D> reconstructPath(Node node) {
    List<Point2D> path = new ArrayList<>();
    while (node != null) {
      path.add(new Point2D(node.x, node.y));
      node = node.parent;
    }
    Collections.reverse(path);
    return path;
  }
}
