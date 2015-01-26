package org.hawkular.inventory.titanPoc;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.codehaus.groovy.runtime.powerassert.SourceText;
import org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain.TitanInventoryEdge;
import org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain.TitanInventoryGraph;
import org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain.TitanInventoryNode;
import org.openjdk.jmh.annotations.*;

import java.util.List;

/**
 * Created by jkremser on 1/19/15.
 */
@State(value = Scope.Thread)
public abstract class AbstractTitanGraphBenchmark extends AbstractGraphBenchmark<TitanInventoryGraph> {

    public static final String MARKER = "isCreated";
    public static final int ITERATIONS_NUMBER = 3;
    public static final int WARMUPS_NUMBER = 1;
    private TitanInventoryGraph inventoryGraph;

    @Setup
    @Override
    public void setupGraph() {
        System.out.println("setup...");
        Configuration conf = getConfiguration();
        TitanGraph graph = TitanFactory.open(conf);
        TitanInventoryGraph inventoryGraph = new TitanInventoryGraph(graph);

        boolean initialized = graph.getVertices("name", MARKER).iterator().hasNext();
        if (!initialized) {
            Vertex vertex = graph.addVertex(MARKER);
            vertex.setProperty("name", MARKER);
            insertSimpleInventory(inventoryGraph);
        }
        this.inventoryGraph = inventoryGraph;
    }

    @TearDown
    @Override
    public void cleanGraph() {
        System.out.println("teardown...");
        List<TitanInventoryNode> nodes = inventoryGraph.nodes();
        nodes.forEach(node -> node.getNode().remove());
        inventoryGraph.getGraph().shutdown();
    }

    @Override
    protected abstract void run();

    protected void insertNodes(TitanInventoryGraph inventoryGraph){

    }

    protected void insertNodes(TitanInventoryGraph inventoryGraph, GraphStructure structure) {
        switch (structure) {
            case SIMPLE_INVENTORY:
                insertSimpleInventory(inventoryGraph);
                break;
            default:
                throw new UnsupportedOperationException("unknown graph structure: " + structure);
        }
    }

    protected void insertSimpleInventory(TitanInventoryGraph inventoryGraph) {
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

    protected Configuration getConfiguration() {
        Configuration conf = new BaseConfiguration();
        conf.setProperty("storage.backend", "cassandra");
        conf.setProperty("storage.hostname", "127.0.0.1");
        return conf;
    }

    enum GraphStructure {
        SIMPLE_INVENTORY, RANDOM
    }

    public TitanInventoryGraph getInventoryGraph() {
        return inventoryGraph;
    }
}
