package org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain;

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
    public String property(String key) {
        return edge.getProperty(key);
    }

    public Edge getEdge() {
        return edge;
    }

    public TitanInventoryEdge(Edge edge) {
        this.edge = edge;
    }
}
