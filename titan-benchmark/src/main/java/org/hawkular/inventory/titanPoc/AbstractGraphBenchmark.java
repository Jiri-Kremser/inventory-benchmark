package org.hawkular.inventory.titanPoc;

import org.hawkular.inventory.titanPoc.graphDomain.InventoryGraph;

/**
 * Created by jkremser on 1/19/15.
 */
public abstract class AbstractGraphBenchmark<T extends InventoryGraph> {
    abstract void setupGraph();
    abstract void cleanGraph();
    abstract void run();

    public void start() {
        try {
            setupGraph();
            run();
        } finally {
            cleanGraph();
        }
    }
}
