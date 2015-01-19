package org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain;

/**
 * Created by jkremser on 1/19/15.
 */
public interface InventoryEdge<T extends InventoryNode> {
    T source();

    T target();

    String label();

    String property(String key);
}
