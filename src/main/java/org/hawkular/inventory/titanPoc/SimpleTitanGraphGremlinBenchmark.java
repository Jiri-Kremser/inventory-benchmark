package org.hawkular.inventory.titanPoc;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.groovy.Gremlin;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.iterators.SingleIterator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * Created by jkremser on 1/21/15.
 */
@Fork(2)
@Warmup(iterations = 1, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.SingleShotTime)
public class SimpleTitanGraphGremlinBenchmark extends AbstractTitanGraphBenchmark {

    @Override
    protected void run() {
        testGremlinSimpleQuery();
        testGremlinCompiledQuery();
    }

    @Benchmark
    public void testGremlinSimpleQuery() {
        System.out.printf("What is deplyed on WildFly?");
        Pipe<Vertex, Vertex> pipe = new GremlinPipeline<Vertex, Vertex>(getInventoryGraph().getGraph().getVertices())
                .as("node").out("isDeployedOn").has("name", "wildfly").back("node").cast(Vertex.class);
        pipe.forEach(node -> System.out.println("node: " + node.getProperty("name")));
    }

    @Benchmark
    public void testGremlinCompiledQuery() {
        System.out.printf("Children of host:");
        GremlinPipeline<Vertex, ? extends Element> pipe1 = new GremlinPipeline<Vertex, Vertex>(getInventoryGraph().getGraph().getVertices())
                .has("name", "host");

        pipe1.forEach(node -> {
            Pipe pipe2 = Gremlin.compile("_().in('isChildOf').name");
            pipe2.setStarts(new SingleIterator<>((Vertex)node));
            pipe2.forEach(System.out::println);
        });
    }
}
