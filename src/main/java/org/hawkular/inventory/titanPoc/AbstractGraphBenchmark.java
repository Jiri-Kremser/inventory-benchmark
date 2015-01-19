package org.hawkular.inventory.titanPoc;

import org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain.InventoryGraph;

/**
 * Created by jkremser on 1/19/15.
 */
public abstract class AbstractGraphBenchmark<T extends InventoryGraph> {
    abstract protected T setupGraph();
    abstract protected void cleanGraph(T graph);
    abstract protected void run(T graph);

    public void start() {
        T graph = null;
        try {
            graph = setupGraph();
            run(graph);
        } finally {
            cleanGraph(graph);
        }
    }
}
