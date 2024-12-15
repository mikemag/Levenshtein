import java.util.*;

public class ConnectedGraph {
    public static ArrayList<LinkedList<Integer>> allPathsIn (int wordIndex, LevenshteinDatabase database) {
        ArrayList<LinkedList<Integer>> pathList = new ArrayList();
        HashSet<Integer> foundWords = new HashSet<Integer>();
        foundWords.add(wordIndex);

        ArrayList<ArrayList<ConnectedNode>> graph = new ArrayList();
        graph.add(new ArrayList());
        graph.get(0).add(new ConnectedNode(wordIndex, new LinkedList(), pathList));

        for (int i = 0; graph.get(i).size() != 0; i++) {
            graph.add(i + 1, new ArrayList());

            for (ConnectedNode node : graph.get(i)) {
                node.addChildren(database, graph.get(i + 1), foundWords, pathList);
            }

            for (ConnectedNode node : graph.get(i + 1)) {
                foundWords.add(node.wordIndex);
            }
        }

        return pathList;
    }
}

class ConnectedNode {
    public final int wordIndex;
    private final LinkedList<Integer> path;
    public ConnectedNode(int wordIndex, LinkedList<Integer> path, ArrayList<LinkedList<Integer>> pathList) {
        path.addLast(wordIndex);
        pathList.add(path);
        this.wordIndex = wordIndex;
        this.path = path;
    }

    public void addChildren(LevenshteinDatabase database, ArrayList<ConnectedNode> frontier, HashSet<Integer> searched, ArrayList<LinkedList<Integer>> pathList) {
        Integer[] children = database.findNeighbors(wordIndex);
        Arrays.asList(children).removeAll(searched);

        for (int child : children) {
            frontier.add(new ConnectedNode(child, new LinkedList(path), pathList));
        }
    }
}
