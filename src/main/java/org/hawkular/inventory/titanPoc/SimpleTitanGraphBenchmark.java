package org.hawkular.inventory.titanPoc;

import org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain.TitanInventoryGraph;
import org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain.TitanInventoryNode;

import java.util.List;

/**
 * Created by jkremser on 1/19/15.
 */
public class SimpleTitanGraphBenchmark extends AbstractGraphBenchmark<TitanInventoryGraph> {
    @Override
    protected TitanInventoryGraph setupGraph() {
        TitanInventoryGraph inventoryGraph = new TitanInventoryGraph();
        return inventoryGraph;
    }

    @Override
    protected void cleanGraph(TitanInventoryGraph graph) {
        List<TitanInventoryNode> nodes = graph.nodes();
        nodes.forEach(node -> node.getNode().remove());
    }

    @Override
    protected void run(TitanInventoryGraph graph) {

    }
}
