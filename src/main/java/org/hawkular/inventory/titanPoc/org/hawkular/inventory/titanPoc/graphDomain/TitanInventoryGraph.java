package org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by jkremser on 1/19/15.
 */
public class TitanInventoryGraph implements InventoryGraph<TitanInventoryNode> {
    private TitanGraph graph;

    @Override
    public List<TitanInventoryNode> nodes() {
        Stream<Vertex> stream = StreamSupport.stream(graph.getVertices().spliterator(), false);
        List<TitanInventoryNode> list = stream.map(n -> new TitanInventoryNode(n)).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<TitanInventoryEdge> edges() {
        Stream<Edge> stream = StreamSupport.stream(graph.getEdges().spliterator(), false);
        List<TitanInventoryEdge> list = stream.map(e -> new TitanInventoryEdge(e)).collect(Collectors.toList());
        return list;
    }

    @Override
    public String label() {
        return "Titan Graph";
    }

    @Override
    public void addNode(TitanInventoryNode node) {
        Vertex newNode = graph.addVertex();
        node.getPropertyKeys().forEach(key -> newNode.setProperty(key, node.getProperty(key)));
    }

    @Override
    public void addEdge(TitanInventoryNode source, TitanInventoryNode target, String label) {
        graph.addEdge(null, source.getNode(), target.getNode(), label);
    }
}
