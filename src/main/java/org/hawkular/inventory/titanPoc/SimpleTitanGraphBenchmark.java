package org.hawkular.inventory.titanPoc;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.groovy.Gremlin;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.iterators.SingleIterator;
import org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain.TitanInventoryEdge;
import org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain.TitanInventoryGraph;
import org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain.TitanInventoryNode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by jkremser on 1/21/15.
 */
@Fork(2)
@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.SingleShotTime)
public class SimpleTitanGraphBenchmark extends AbstractTitanGraphBenchmark {

    @Override
    protected void run() {
        testBasicBlueprintsApiWithSout();
        testBasicBlueprintsApi();
        testBasicBlueprintsApiForAllNodesFindSomethingComplex();
        testBasicBlueprintsApiGraphQuerying();
        testBasicBlueprintsApiForAllEdgesAddTheReverseOne();
    }

    @Benchmark
    public void testBasicBlueprintsApiWithSout() {
        List<TitanInventoryNode> nodes = getInventoryGraph().nodes();
        nodes.forEach(node -> {
            System.out.println("\nNode " + node.getProperty("name"));
            Iterable<Edge> edges = node.getNode().getEdges(Direction.OUT);
            System.out.println("out edges:");
            edges.forEach(edge -> System.out.println("--" + edge.getLabel() + "--> " + edge.getVertex(Direction.IN).getProperty("name")));
        });
    }

    @Benchmark
    public String testBasicBlueprintsApi() {
        // we need to use/return the result of computation to avoid dead code elimination
        final StringBuilder sb = new StringBuilder();
        List<TitanInventoryNode> nodes = getInventoryGraph().nodes();
        nodes.forEach(node -> {
            Iterable<Edge> edges = node.getNode().getEdges(Direction.OUT);
            edges.forEach(edge -> {
                sb.append(edge.getLabel());
                sb.append(edge.getVertex(Direction.IN).getProperty("name").toString());
            });
        });
        return sb.toString();
    }

    @Benchmark
    public String testBasicBlueprintsApiForAllNodesFindSomethingComplex() {
        // we need to use/return the result of computation to avoid dead code elimination
        final StringBuilder sb = new StringBuilder();
        List<TitanInventoryNode> nodes = getInventoryGraph().nodes();
        nodes.forEach(node -> {
            System.out.println(node.getProperty("name"));
            if (node.getNode().query().labels("isChildOf").count() > 0) {
                System.out.println("yes: " + node.getProperty("name"));
                String foo = StreamSupport.stream(node.getNode().getVertices(Direction.BOTH).spliterator(), false)
                        .filter(vertex -> vertex.getProperty("name").toString().startsWith("rhq-m"))
                        .map(vertex -> vertex.getProperty("name").toString())
                        .sorted()
                        .collect(Collectors.joining(", "));
                if (!foo.isEmpty()) {
                    sb.append(node.getProperty("name")).append(": ").append(foo).append("\n");
                }
            }
        });
        String ret = sb.toString();
        System.out.println("result= " + ret);
        return ret;
    }

    @Benchmark
    public String testBasicBlueprintsApiForAllEdgesAddTheReverseOne() {
        // we need to use/return the result of computation to avoid dead code elimination
        final StringBuilder sb = new StringBuilder();
        List<TitanInventoryEdge> edges = getInventoryGraph().edges();
        String reversedRelations = StreamSupport.stream(edges.spliterator(), false)
                .map(edge -> {
                    TitanInventoryNode source = edge.source();
                    TitanInventoryNode target = edge.target();
                    // add edge as an explicit side effect of map
                    TitanInventoryEdge reverseEdge = getInventoryGraph().addEdge(target, source, "reversed relation of " + edge.label());
                    sb.append(reverseEdge.label());
                    return reverseEdge.label();
                })
                .collect(Collectors.joining("\n"));

        System.out.println(reversedRelations);
        return sb.toString();
    }

    @Benchmark
    public void testBasicBlueprintsApiGraphQuerying() {
        StreamSupport.stream(getInventoryGraph().getGraph().query().has("name", "RHQ Metrics DS").vertices().spliterator(), false).findFirst().ifPresent(System.out::println);
    }


    @Benchmark
    public void testBGremlin1() {
        Pipe pipe = Gremlin.compile("_().out('knows').name");
        pipe.setStarts(new SingleIterator<Vertex>(getInventoryGraph().getGraph().getVertex(1)));
        for(Object name : pipe) {
            System.out.println((String) name);
        }
    }

    @Benchmark
    public void testBGremlin2() {
        Pipe pipe = new GremlinPipeline<Vertex, Vertex>(getInventoryGraph().getGraph().getVertices())
                .as("node").out("isDeployedOn").has("name", "wildfly").back("node").cast(Vertex.class);
        for(Object name : pipe) {
            System.out.println((String) name);
        }
    }
}
