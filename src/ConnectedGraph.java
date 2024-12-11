import java.util.*;

public class ConnectedGraph {
    public static ArrayList<LinkedList<String>> allPathsIn (String word, LevenshteinDatabase database) {
        ArrayList<LinkedList<String>> pathList = new ArrayList();
        HashSet<String> foundWords = new HashSet<String>();
        foundWords.add(word);

        ArrayList<ArrayList<ConnectedNode>> graph = new ArrayList();
        graph.add(new ArrayList());
        graph.get(0).add(new ConnectedNode(word, new LinkedList(), pathList));

        for (int i = 0; graph.get(i).size() != 0; i++) {
            graph.add(i + 1, new ArrayList());

            for (ConnectedNode node : graph.get(i)) {
                node.addChildren(database, graph.get(i + 1), foundWords, pathList);
            }

            for (ConnectedNode node : graph.get(i + 1)) {
                foundWords.add(node.word);
            }
        }

        return pathList;
    }
}

class ConnectedNode {
    public final String word;
    private final LinkedList<String> path;
    public ConnectedNode(String word, LinkedList<String> path, ArrayList<LinkedList<String>> pathList) {
        path.addLast(word);
        pathList.add(path);
        this.word = word;
        this.path = path;
    }

    public void addChildren(LevenshteinDatabase database, ArrayList<ConnectedNode> frontier, HashSet<String> searched, ArrayList<LinkedList<String>> pathList) {
        HashSet<String> children = database.findNeighbors(word);
        children.removeAll(searched);

        for (String child : children) {
            frontier.add(new ConnectedNode(child, new LinkedList(path), pathList));
        }
    }
}
