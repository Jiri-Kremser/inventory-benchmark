package org.hawkular.inventory.titanPoc.graphDomain;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;

/**
 * Created by jkremser on 1/19/15.
 */
public class TitanInventoryEdge implements InventoryEdge<TitanInventoryNode> {
    private Edge edge;

    @Override
    public TitanInventoryNode source() {
        return new TitanInventoryNode(edge.getVertex(Direction.IN));
    }

    @Override
    public TitanInventoryNode target() {
        return new TitanInventoryNode(edge.getVertex(Direction.OUT));
    }

    @Override
    public String label() {
        return edge.getLabel();
    }

    @Override
    public String getProperty(String key) {
        return edge.getProperty(key);
    }

    @Override
    public void setProperty(String key, String property) {
        edge.setProperty(key, property);
    }

    public Edge getEdge() {
        return edge;
    }

    public TitanInventoryEdge(Edge edge) {
        this.edge = edge;
        edge.getPropertyKeys().forEach(key -> setProperty(key, edge.getProperty(key)));
    }
}
