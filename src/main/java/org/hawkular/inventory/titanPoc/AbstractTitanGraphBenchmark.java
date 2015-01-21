package org.hawkular.inventory.titanPoc;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.codehaus.groovy.runtime.powerassert.SourceText;
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

    public abstract void insertSimpleInventory(TitanInventoryGraph inventoryGraph) ;

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
