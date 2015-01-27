package org.hawkular.inventory.titanPoc.graphDomain;

import com.tinkerpop.blueprints.Vertex;

import java.util.Set;

/**
 * Created by jkremser on 1/19/15.
 */
public class TitanInventoryNode implements InventoryNode {
    private Vertex node;

    @Override
    public Object id() {
        return node.getId();
    }

    @Override
    public String getProperty(String key) {
        return node.getProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return node.getPropertyKeys();
    }

    @Override
    public void setProperty(String key, String property) {
        node.setProperty(key, property);
    }


    public Vertex getNode() {
        return node;
    }

    public TitanInventoryNode(Vertex node) {
        this.node = node;
        node.getPropertyKeys().forEach(key -> setProperty(key, node.getProperty(key)));
    }
}
