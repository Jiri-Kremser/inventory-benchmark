package org.rhq;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.Random;

/**
 * Created by jkremser on 12/4/14.
 * <p>
 * theory - https://en.wikipedia.org/wiki/Erd%C5%91s%E2%80%93R%C3%A9nyi_model
 * <p>
 * There are 2 params n - number of nodes and p - probability of forming an edge between 2 nodes.
 * if p >= 2ln(n)/n then almost every randomly generated graph is connected.
 * For n=10000 its 0.00184
 */
public class RandomGraphDemo {
    public static final String MARKER = "isCreated";
    // http://listofrandomwords.com
    public static final String[] RANDOM_WORDS = {"stime", "strand", "sech", "sodden", "strep", "santal", "skivvy", "sifaka", "skolly",
            "shirt", "stint", "stogy", "spinal", "swept", "sempre", "sal", "samsun", "skull", "slough", "saving", "spadix", "shole",
            "shilh", "spooky", "shiai", "soli", "stoit", "sulpha", "somoza", "seal", "septa", "stops", "sippet", "segue", "surely",
            "sitar", "shall", "sateen", "six", "spital", "sonata", "sayer", "shoa", "soffit", "sawfly", "stadia", "sem", "slated", "steier",
            "skirt", "strait", "scopes", "scilla", "stum", "schuss", "solve", "stripe", "suck", "shrink", "snod", "scion", "sodom", "surety",
            "succor", "smeeky", "sruti", "selah", "shoo", "spica", "sis", "shawm", "sheath", "stanch", "sambur", "seine", "slope", "sett", "spiffy",
            "simms", "stingy", "steeve", "sister", "sesame", "slyly", "strake", "sudra", "scotus", "surra", "shoji", "sabean", "sikh", "secam",
            "satay", "salpa", "swan", "spigot", "sayid", "shout", "stodge", "swart"};

    public static void main(String[] args) {
        long initPhaseDuration = -1, queryPhase1Duration = -1, queryPhase2Duration = -1, deletingDuration = -1;

        boolean remove = true;
        int verticesNumber = 10000;
        float edgeProbability = (float) (2.0f*Math.log(verticesNumber)/(float)verticesNumber);
        long seed = new Random().nextLong();
        try {
            if (args.length > 0) {
                verticesNumber = Integer.parseInt(args[0]);
                if (args.length > 1) {
                    edgeProbability = Float.parseFloat(args[1]);
                    if (args.length > 2) {
                        seed = Long.parseLong(args[3]);
                    }
                }
            }
        } catch (NumberFormatException nfe) {
            // nothing
            //System.err.println("Bad format, using defaults");
        }
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend", "cassandra");
        conf.setProperty("storage.hostname", "127.0.0.1");
        TitanGraph graph = TitanFactory.open(conf);
        boolean initialized = graph.getVertices("name", MARKER).iterator().hasNext();
        // INIT
        if (!initialized) {
            System.out.println("\n****************\ninitializing the graph with " + verticesNumber + " vertices and edge probability " + edgeProbability);
            long start = System.currentTimeMillis();
            setupGraph(graph, verticesNumber, edgeProbability, seed);
            graph.commit();
            initPhaseDuration = System.currentTimeMillis() - start;
            System.out.println("Initialization took " + initPhaseDuration + "ms");
        }

        // QUERY 1
        long start = System.currentTimeMillis();
        Iterable<Vertex> vertices = graph.getVertices();
        vertices.forEach((vertex) -> {
            System.out.println("\nNode " + vertex.getProperty("name"));
            Iterable<Edge> edges = vertex.getEdges(Direction.OUT);
            System.out.println("out edges:");
            edges.forEach((edge) -> System.out.println("--" + edge.getLabel() + "--> " + edge.getVertex(Direction.IN).getProperty("name")));
        });
        queryPhase1Duration = System.currentTimeMillis() - start;
        System.out.println("Query phase 1 took " + queryPhase1Duration + "ms");


        // QUERY 2
        start = System.currentTimeMillis();
        vertices = graph.getVertices();
        vertices.forEach((vertex) -> {
            vertex.getProperty("name"); // call and forget
            Iterable<Edge> edges = vertex.getEdges(Direction.OUT);
            edges.forEach((edge) -> {
                edge.getLabel(); // call and forget
                edge.getVertex(Direction.IN).getProperty("name"); // call and forget
            });
        });
        queryPhase2Duration = System.currentTimeMillis() - start;
        System.out.println("Query phase 2 took " + queryPhase2Duration + "ms");

        // DELETE
        if (remove) {
            System.out.println("\n****************\nremoving the graph");
            start = System.currentTimeMillis();
            removeGraph(graph);
            graph.commit();
            deletingDuration = System.currentTimeMillis() - start;
            System.out.println("Removing took " + deletingDuration + "ms");
        }
        graph.shutdown();

        System.out.println("\n****************\ntimes taken (n = " + verticesNumber+  ", p =" + edgeProbability +"):\n****************");
        initPhaseDuration = Math.max(0, initPhaseDuration);
        deletingDuration = Math.max(0, deletingDuration);
        System.out.println(initPhaseDuration + "ms for creating the graph");
        System.out.println(queryPhase1Duration + "ms for querying the graph");
        System.out.println(queryPhase2Duration + "ms for querying (without System.out.println) the graph");
        System.out.println(deletingDuration + "ms for deleting the graph");
    }

    public static void setupGraph(Graph graph, final int verticesNumber, final float edgeProbability, final long seed) {
        // vertices
        Vertex[] vertices = new Vertex[verticesNumber];
        Random rand = new Random(seed);
        Vertex vertex = graph.addVertex(MARKER);
        vertex.setProperty("name", MARKER);
        for (int i = 0; i < verticesNumber; i++) {
            vertex = graph.addVertex(i);
            vertex.setProperty("name", RANDOM_WORDS[rand.nextInt(RANDOM_WORDS.length)]);
            vertices[i] = vertex;
            for (int j = 0; j < i; j++) {
                // j < i -> no self loops
                if (rand.nextFloat() < edgeProbability) {
                    // we are lucky
                    graph.addEdge(i + "#" + j, vertex, vertices[j], RANDOM_WORDS[rand.nextInt(RANDOM_WORDS.length)]);
                }
            }
        }
    }

    public static void removeGraph(Graph graph) {
        Iterable<Vertex> vertices = graph.getVertices();
        vertices.forEach((vertex) -> vertex.remove());
    }
}
