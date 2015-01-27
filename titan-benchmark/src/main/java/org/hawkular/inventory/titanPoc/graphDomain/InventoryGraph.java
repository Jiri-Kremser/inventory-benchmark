package org.hawkular.inventory.titanPoc.graphDomain;

import java.util.List;

/**
 * Created by jkremser on 1/19/15.
 */
public interface InventoryGraph<T extends InventoryNode> {
    List<T> nodes();
    List<? extends InventoryEdge<T>> edges();
    String label();
    T addNode(String label);
    InventoryEdge<T> addEdge(T source, T target, String label);
}
