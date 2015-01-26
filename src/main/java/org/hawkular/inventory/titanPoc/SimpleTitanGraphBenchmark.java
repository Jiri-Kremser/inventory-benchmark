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




    @Override
    public void insertSimpleInventory(TitanInventoryGraph inventoryGraph) {
        // vertices
        TitanInventoryNode host = inventoryGraph.addNode("host");

        host.setProperty("name", "host");
        host.setProperty("ip", "127.0.0.1");
        host.setProperty("hostname", "localhost");
        host.setProperty("os", "Linux");

        TitanInventoryNode wildfly1 = inventoryGraph.addNode("wildfly1");
        wildfly1.setProperty("name", "wildfly");
        wildfly1.setProperty("version", "8.0 GA");
        wildfly1.setProperty("pid", "42");

        TitanInventoryNode wildfly2 = inventoryGraph.addNode("wildfly2");
        wildfly2.setProperty("name", "wildfly");
        wildfly2.setProperty("version", "8.0 GA");
        wildfly2.setProperty("pid", "43");

        TitanInventoryNode lb = inventoryGraph.addNode("load-balancer");
        lb.setProperty("name", "load-balancer");
        lb.setProperty("method", "httpd+mod_cluster");
        lb.setProperty("url", "http://127.0.0.1");
        lb.setProperty("pid", "44");

        TitanInventoryNode rhqMetrics1 = inventoryGraph.addNode("rhq-metrics1");
        rhqMetrics1.setProperty("name", "rhq-metrics");
        rhqMetrics1.setProperty("url", "http://127.0.0.1:8080");

        TitanInventoryNode rhqMetricsDS1 = inventoryGraph.addNode("ds1");
        rhqMetricsDS1.setProperty("name", "RHQ Metrics DS");
        rhqMetricsDS1.setProperty("connection-url", "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1");

        TitanInventoryNode rhqMetrics2 = inventoryGraph.addNode("rhq-metrics2");
        rhqMetrics2.setProperty("name", "rhq-metrics");
        rhqMetrics2.setProperty("url", "http://127.0.0.1:8081");

        TitanInventoryNode rhqMetricsDS2 = inventoryGraph.addNode("ds2");
        rhqMetricsDS2.setProperty("name", "RHQ Metrics DS");
        rhqMetricsDS2.setProperty("connection-url", "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1");

        TitanInventoryNode db = inventoryGraph.addNode("DB");
        db.setProperty("name", "database");
        db.setProperty("vendor", "H2");
        db.setProperty("pid", "42");

        TitanInventoryNode oldDb = inventoryGraph.addNode("oldDB");
        oldDb.setProperty("name", "database");
        oldDb.setProperty("vendor", "Postgres");

        TitanInventoryNode rhqMetricsApp = inventoryGraph.addNode("rhq-metrics-app");
        rhqMetricsApp.setProperty("name", "Metrics");
        rhqMetricsApp.setProperty("url", "http://127.0.0.1");
        rhqMetricsApp.setProperty("version", "1.0");

        TitanInventoryNode cpu1 = inventoryGraph.addNode("CPU1");
        cpu1.setProperty("name", "CPU1");
        cpu1.setProperty("nodel-name", "Intel(R) Core(TM) i7-2620M CPU @ 2.70GHz");
        cpu1.setProperty("cpu-cores", "2");

        TitanInventoryNode cpu2 = inventoryGraph.addNode("CPU2");
        cpu2.setProperty("name", "CPU1");
        cpu2.setProperty("nodel-name", "Intel(R) Core(TM) i7-2620M CPU @ 2.70GHz");
        cpu2.setProperty("cpu-cores", "2");

        TitanInventoryNode ram = inventoryGraph.addNode("RAM");
        ram.setProperty("name", "RAM");
        ram.setProperty("total", "16314444 kB");

        // edges
        inventoryGraph.addEdge(wildfly1, host, "isChildOf").setProperty("weight", "1.0f");
        inventoryGraph.addEdge(wildfly2, host, "isChildOf").setProperty("weight", "1.0f");
        inventoryGraph.addEdge(lb, host, "isChildOf").setProperty("weight", "1.0f");
        inventoryGraph.addEdge(cpu1, host, "isChildOf").setProperty("weight", "1.0f");
        inventoryGraph.addEdge(cpu2, host, "isChildOf").setProperty("weight", "1.0f");
        inventoryGraph.addEdge(ram, host, "isChildOf").setProperty("weight", "1.0f");
        inventoryGraph.addEdge(db, host, "isChildOf").setProperty("weight", "1.0f");

        inventoryGraph.addEdge(rhqMetrics1, wildfly1, "isDeployedOn").setProperty("weight", "0.95f");
        inventoryGraph.addEdge(rhqMetricsDS1, wildfly1, "isDeployedOn").setProperty("weight", "0.95f");

        inventoryGraph.addEdge(rhqMetrics2, wildfly2, "isDeployedOn").setProperty("weight", "0.95f");
        inventoryGraph.addEdge(rhqMetricsDS2, wildfly2, "isDeployedOn").setProperty("weight", "0.95f");

        inventoryGraph.addEdge(rhqMetrics1, rhqMetricsDS1, "uses").setProperty("weight", "1.0f");
        inventoryGraph.addEdge(rhqMetrics2, rhqMetricsDS2, "uses").setProperty("weight", "1.0f");

        inventoryGraph.addEdge(rhqMetricsDS1, db, "requires").setProperty("weight", "1.0f");
        inventoryGraph.addEdge(rhqMetricsDS2, db, "requires").setProperty("weight", "1.0f");

        // some funky stuff with edge params
        TitanInventoryEdge e = inventoryGraph.addEdge(rhqMetricsApp, rhqMetrics1, "consistOf");
        e.setProperty("weight", "0.25f");
        e.setProperty("since", "2014-12-04");
        e = inventoryGraph.addEdge(rhqMetricsApp, rhqMetrics2, "consistOf");
        e.setProperty("weight", "0.25f");
        e.setProperty("since", "2014-12-04");
        e = inventoryGraph.addEdge(rhqMetricsApp, lb, "consistOf");
        e.setProperty("weight", "0.25f");
        e.setProperty("since", "2014-12-04");
        e = inventoryGraph.addEdge(rhqMetricsApp, db, "consistOf");
        e.setProperty("weight", "0.25f");
        e.setProperty("since", "2014-12-04");

        e = inventoryGraph.addEdge(rhqMetricsApp, oldDb, "consistOf");
        e.setProperty("weight", "0.25f");
        e.setProperty("since", "2013-12-01");
        e.setProperty("till", "2014-12-03");
    }
}
