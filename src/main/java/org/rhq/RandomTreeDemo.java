package org.rhq;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.util.Random;

/**
 * Created by jkremser on 12/8/14.
 * <p>
 * theory - https://en.wikipedia.org/wiki/Erd%C5%91s%E2%80%93R%C3%A9nyi_model
 * <p>
 * There are 2 params n - number of nodes and p - probability of forming an edge between 2 nodes.
 * if p >= 2ln(n)/n then almost every randomly generated graph is connected.
 * For n=10000 its 0.00184
 */
public class RandomTreeDemo extends RandomGraphDemo {
    public static final String ROOT = "root";
    private int edgeCounter;

    public static void main(String[] args) {
        RandomTreeDemo demo = new RandomTreeDemo();
        demo.run(args);
    }

    public void run(String[] args) {
        System.out.println("\n\nRandomGraphDemo specific tests");
        super.run(args, false);
        System.out.println("\n\nRandomTreeDemo specific tests");
        long start = System.currentTimeMillis();
        Vertex root = getGraph().getVertices("name", ROOT).iterator().next();
        System.out.println("\nlooking up the root node took " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        edgeCounter = 0;
        root.getEdges(Direction.OUT).forEach((edge) -> {
            edge.getLabel();
            edgeCounter++;
        });
        long end = System.currentTimeMillis();
        System.out.println("\ncounting all out-edges from root took " + (end - start) + "ms");
        System.out.println("# of all outgoing edges is " + edgeCounter);

        edgeCounter = 0;
        start = System.currentTimeMillis();
        root.getEdges(Direction.OUT, RANDOM_WORDS[0]).forEach((edge) -> {
            edge.getLabel();
            edgeCounter++;
        });
        end = System.currentTimeMillis();
        System.out.println("\ncounting filtered out-edges from root took " + (end - start) + "ms");
        System.out.println("# of filtered outgoing edges is " + edgeCounter);

        removeGraph();
        getGraph().commit();
        getGraph().shutdown();
    }

    @Override
    public void setupGraph(Graph graph, final int verticesNumber, final float edgeProbability, final long seed) {
        // vertices
        Vertex[] vertices = new Vertex[verticesNumber];
        Random rand = new Random(seed);
        Vertex vertex = graph.addVertex(MARKER);
        vertex.setProperty("name", MARKER);
        for (int i = 0; i < verticesNumber; i++) {
            vertex = graph.addVertex(i);
            vertex.setProperty("name", i == 0 ? ROOT : RANDOM_WORDS[rand.nextInt(RANDOM_WORDS.length)]);
            vertices[i] = vertex;
            for (int j = 0; j < i; j++) {
                // j < i -> no self loops
                if (rand.nextFloat() < edgeProbability) {
                    // we are lucky
                    graph.addEdge(i + "#" + j, vertices[j], vertex, RANDOM_WORDS[rand.nextInt(RANDOM_WORDS.length)]);
                    break;
                }
            }
        }
    }
}
