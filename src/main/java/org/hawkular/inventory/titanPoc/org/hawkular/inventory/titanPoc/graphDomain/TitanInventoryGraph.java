package org.hawkular.inventory.titanPoc.org.hawkular.inventory.titanPoc.graphDomain;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

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
    public TitanInventoryNode addNode(String label) {
        Vertex newNode = graph.addVertex(label);
        return new TitanInventoryNode(newNode);
    }

    @Override
    public TitanInventoryEdge addEdge(TitanInventoryNode source, TitanInventoryNode target, String label) {
        return new TitanInventoryEdge(graph.addEdge(null, source.getNode(), target.getNode(), label));
    }

    public TitanInventoryGraph(TitanGraph graph) {
        this.graph = graph;
    }

    public TitanGraph getGraph() {
        return graph;
    }
}
