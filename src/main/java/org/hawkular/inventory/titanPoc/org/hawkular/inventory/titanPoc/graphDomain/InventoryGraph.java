package org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain;

import java.util.List;

/**
 * Created by jkremser on 1/19/15.
 */
public interface InventoryGraph<T extends InventoryNode> {
    List<T> nodes();
    List<? extends InventoryEdge<T>> edges();
    String label();
    void addNode(T node);
    void addEdge(T source, T target, String label);
}
