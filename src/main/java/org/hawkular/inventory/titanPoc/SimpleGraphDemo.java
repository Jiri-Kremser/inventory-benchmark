package org.hawkular.inventory.titanPoc;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Created by jkremser on 12/4/14.
 */
public class SimpleGraphDemo {
    public static final String APP_NAME = "rhq-metrics-app";

    public static void main(String[] args) {
        boolean remove = true;

        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend","cassandra");
        conf.setProperty("storage.hostname","127.0.0.1");
        TitanGraph graph = TitanFactory.open(conf);
        boolean initialized = graph.getVertices("name", APP_NAME).iterator().hasNext();
        if (!initialized) {
            System.out.println("initializing the graph");
            setupGraph(graph);
            graph.commit();
        }
        Iterable<Vertex> vertices = graph.getVertices();
        vertices.forEach((vertex) -> {
            System.out.println("\nNode " + vertex.getProperty("name"));
            Iterable<Edge> edges = vertex.getEdges(Direction.OUT);
            System.out.println("out edges:");
            edges.forEach((edge) -> System.out.println("--" + edge.getLabel() + "--> " + edge.getVertex(Direction.IN).getProperty("name")));
        });

        if (remove) {
            System.out.println("removing the graph");
            removeGraph(graph);
            graph.commit();
        }
        graph.shutdown();
    }

//    @Benchmark
//    @BenchmarkMode({Mode.Throughput})
    public void test1() throws Exception {
        Constructor<ArrayList> constructor = ArrayList.class.getDeclaredConstructor(int.class);
        constructor.newInstance(42);
    }

//    @Benchmark
//    @BenchmarkMode(Mode.Throughput)
    public void test2() throws Exception {
        new ArrayList(42);
    }

    public static void setupGraph(Graph graph) {
        // vertices
        Vertex host = graph.addVertex("host");
        // ..or TitanGraphTransaction.addVertexWithLabel(VertexLabel foo) ?

        host.setProperty("name", "host");
        host.setProperty("ip", "127.0.0.1");
        host.setProperty("hostname", "localhost");
        host.setProperty("os", "Linux");

        Vertex wildfly1 = graph.addVertex("wildfly1");
        wildfly1.setProperty("name", "wildfly");
        wildfly1.setProperty("version", "8.0 GA");
        wildfly1.setProperty("pid", 42);

        Vertex wildfly2 = graph.addVertex("wildfly2");
        wildfly2.setProperty("name", "wildfly");
        wildfly2.setProperty("version", "8.0 GA");
        wildfly2.setProperty("pid", 43);

        Vertex lb = graph.addVertex("load-balancer");
        lb.setProperty("name", "load-balancer");
        lb.setProperty("method", "httpd+mod_cluster");
        lb.setProperty("url", "http://127.0.0.1");
        lb.setProperty("pid", 44);

        Vertex rhqMetrics1 = graph.addVertex("rhq-metrics1");
        rhqMetrics1.setProperty("name", "rhq-metrics");
        rhqMetrics1.setProperty("url", "http://127.0.0.1:8080");

        Vertex rhqMetricsDS1 = graph.addVertex("ds1");
        rhqMetricsDS1.setProperty("name", "RHQ Metrics DS");
        rhqMetricsDS1.setProperty("connection-url", "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1");

        Vertex rhqMetrics2 = graph.addVertex("rhq-metrics2");
        rhqMetrics2.setProperty("name", "rhq-metrics");
        rhqMetrics2.setProperty("url", "http://127.0.0.1:8081");

        Vertex rhqMetricsDS2 = graph.addVertex("ds2");
        rhqMetricsDS2.setProperty("name", "RHQ Metrics DS");
        rhqMetricsDS2.setProperty("connection-url", "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1");

        Vertex db = graph.addVertex("DB");
        db.setProperty("name", "database");
        db.setProperty("vendor", "H2");
        db.setProperty("pid", 42);

        Vertex oldDb = graph.addVertex("oldDB");
        oldDb.setProperty("name", "database");
        oldDb.setProperty("vendor", "Postgres");

        Vertex rhqMetricsApp = graph.addVertex("rhq-metrics-app");
        rhqMetricsApp.setProperty("name", APP_NAME);
        rhqMetricsApp.setProperty("url", "http://127.0.0.1");
        rhqMetricsApp.setProperty("version", "1.0");

        Vertex cpu1 = graph.addVertex("CPU1");
        cpu1.setProperty("name", "CPU1");
        cpu1.setProperty("nodel-name", "Intel(R) Core(TM) i7-2620M CPU @ 2.70GHz");
        cpu1.setProperty("cpu-cores", 2);

        Vertex cpu2 = graph.addVertex("CPU2");
        cpu2.setProperty("name", "CPU1");
        cpu2.setProperty("nodel-name", "Intel(R) Core(TM) i7-2620M CPU @ 2.70GHz");
        cpu2.setProperty("cpu-cores", 2);

        Vertex ram = graph.addVertex("RAM");
        ram.setProperty("name", "RAM");
        ram.setProperty("total", "16314444 kB");

        // edges
        int objectId = 100;
        graph.addEdge(++objectId, wildfly1, host, "isChildOf").setProperty("weight", 1.0f);
        graph.addEdge(++objectId, wildfly2, host, "isChildOf").setProperty("weight", 1.0f);
        graph.addEdge(++objectId, lb, host, "isChildOf").setProperty("weight", 1.0f);
        graph.addEdge(++objectId, cpu1, host, "isChildOf").setProperty("weight", 1.0f);
        graph.addEdge(++objectId, cpu2, host, "isChildOf").setProperty("weight", 1.0f);
        graph.addEdge(++objectId, ram, host, "isChildOf").setProperty("weight", 1.0f);
        graph.addEdge(++objectId, db, host, "isChildOf").setProperty("weight", 1.0f);

        graph.addEdge(++objectId, rhqMetrics1, wildfly1, "isDeployedOn").setProperty("weight", 0.95f);
        graph.addEdge(++objectId, rhqMetricsDS1, wildfly1, "isDeployedOn").setProperty("weight", 0.95f);

        graph.addEdge(++objectId, rhqMetrics2, wildfly2, "isDeployedOn").setProperty("weight", 0.95f);
        graph.addEdge(++objectId, rhqMetricsDS2, wildfly2, "isDeployedOn").setProperty("weight", 0.95f);

        graph.addEdge(++objectId, rhqMetrics1, rhqMetricsDS1, "uses").setProperty("weight", 1.0f);
        graph.addEdge(++objectId, rhqMetrics2, rhqMetricsDS2, "uses").setProperty("weight", 1.0f);

        graph.addEdge(++objectId, rhqMetricsDS1, db, "requires").setProperty("weight", 1.0f);
        graph.addEdge(++objectId, rhqMetricsDS2, db, "requires").setProperty("weight", 1.0f);

        // some funky stuff with edge params
        Edge e = graph.addEdge(++objectId, rhqMetricsApp, rhqMetrics1, "consistOf");
        e.setProperty("weight", 0.25f);
        e.setProperty("since", "2014-12-04");
        e = graph.addEdge(++objectId, rhqMetricsApp, rhqMetrics2, "consistOf");
        e.setProperty("weight", 0.25f);
        e.setProperty("since", "2014-12-04");
        e = graph.addEdge(++objectId, rhqMetricsApp, lb, "consistOf");
        e.setProperty("weight", 0.25f);
        e.setProperty("since", "2014-12-04");
        e = graph.addEdge(++objectId, rhqMetricsApp, db, "consistOf");
        e.setProperty("weight", 0.25f);
        e.setProperty("since", "2014-12-04");

        e = graph.addEdge(++objectId, rhqMetricsApp, oldDb, "consistOf");
        e.setProperty("weight", 0.25f);
        e.setProperty("since", "2013-12-01");
        e.setProperty("till", "2014-12-03");
    }

    public static void removeGraph(Graph graph) {
        Iterable<Vertex> vertices = graph.getVertices();
        vertices.forEach((vertex) -> vertex.remove());
    }
}
