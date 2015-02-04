package org.hawkular.inventory.artificerPoc;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.server.core.api.ArtifactService;
import org.overlord.sramp.server.core.api.QueryService;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Properties;

/**
 * Created by jkremser on 02/04/15.
 */
@State(value = Scope.Thread)
public abstract class AbstractArtificerBenchmark {

    public static final String DEFAULT_ARTIFACT_TYPE = "bbenchmarkk";
    private ArtifactService artifactService;
    private QueryService queryService;

    @Setup
    public void setupGraph() throws Exception {
        System.out.println("setup...");
        Properties jndiProps = new Properties();
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        jndiProps.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
        jndiProps.put("jboss.naming.client.ejb.context", true);
        Context context = new InitialContext(jndiProps);
        artifactService = (ArtifactService) context.lookup(
                "s-ramp-server/ArtifactService!" + ArtifactService.class.getName());
        artifactService.login("artificer", "artificer1!");
        queryService = (QueryService) context.lookup(
                "s-ramp-server/QueryService!" + QueryService.class.getName());
        queryService.login("artificer", "artificer1!");


        System.out.println("initializing graph...");
        insertSimpleInventory();
    }

    private BaseArtifactType addNode(String type, String name, String description) throws Exception {
        ExtendedArtifactType newNode = new ExtendedArtifactType();
        newNode.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        newNode.setExtendedType(type);
        newNode.setName(name);
        if (description != null) {
            newNode.setDescription(description);
        }
        return artifactService.create(newNode);
    }

    private BaseArtifactType addNode(String name) throws Exception {
        return addNode(DEFAULT_ARTIFACT_TYPE, name, null);
    }

    private Relationship addEdge(BaseArtifactType source, BaseArtifactType target, String label, String type) throws Exception {
        System.out.println("adding edge: " + label);
        Relationship relationship = SrampModelUtils.addGenericRelationship(source, label, target.getUuid());
        artifactService.updateMetaData(ArtifactType.valueOf(type), target.getUuid(), target);
        artifactService.updateMetaData(ArtifactType.valueOf(type), source.getUuid(), source);
        return relationship;
    }

    private Relationship addEdge(BaseArtifactType source, BaseArtifactType target, String label) throws Exception {
        return addEdge(source, target, label, DEFAULT_ARTIFACT_TYPE);
    }

    @TearDown
    public void cleanGraph() throws Exception {
        System.out.println("teardown...");
        queryService.login("artificer", "artificer1!");
        List<BaseArtifactType> artifactTypes = queryService.query("/s-ramp/ext/" + DEFAULT_ARTIFACT_TYPE);
        for (BaseArtifactType type : artifactTypes) {
            artifactService.delete(ArtifactType.valueOf(type), type.getUuid());
        }

    }

    protected void insertSimpleInventory() throws Exception {
        // vertices
        BaseArtifactType host = addNode("host");

//        host.setProperty("name", "host");
//        host.setProperty("ip", "127.0.0.1");
//        host.setProperty("hostname", "localhost");
//        host.setProperty("os", "Linux");

        BaseArtifactType wildfly1 = addNode("wildfly1");
//        wildfly1.setProperty("name", "wildfly");
//        wildfly1.setProperty("version", "8.0 GA");
//        wildfly1.setProperty("pid", "42");

        BaseArtifactType wildfly2 = addNode("wildfly2");
//        wildfly2.setProperty("name", "wildfly");
//        wildfly2.setProperty("version", "8.0 GA");
//        wildfly2.setProperty("pid", "43");

        BaseArtifactType lb = addNode("load-balancer");
//        lb.setProperty("name", "load-balancer");
//        lb.setProperty("method", "httpd+mod_cluster");
//        lb.setProperty("url", "http://127.0.0.1");
//        lb.setProperty("pid", "44");

        BaseArtifactType rhqMetrics1 = addNode("rhq-metrics1");
//        rhqMetrics1.setProperty("name", "rhq-metrics");
//        rhqMetrics1.setProperty("url", "http://127.0.0.1:8080");

        BaseArtifactType rhqMetricsDS1 = addNode("ds1");
//        rhqMetricsDS1.setProperty("name", "RHQ Metrics DS");
//        rhqMetricsDS1.setProperty("connection-url", "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1");

        BaseArtifactType rhqMetrics2 = addNode("rhq-metrics2");
//        rhqMetrics2.setProperty("name", "rhq-metrics");
//        rhqMetrics2.setProperty("url", "http://127.0.0.1:8081");

        BaseArtifactType rhqMetricsDS2 = addNode("ds2");
//        rhqMetricsDS2.setProperty("name", "RHQ Metrics DS");
//        rhqMetricsDS2.setProperty("connection-url", "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1");

        BaseArtifactType db = addNode("DB");
//        db.setProperty("name", "database");
//        db.setProperty("vendor", "H2");
//        db.setProperty("pid", "42");

        BaseArtifactType oldDb = addNode("oldDB");
//        oldDb.setProperty("name", "database");
//        oldDb.setProperty("vendor", "Postgres");

        BaseArtifactType rhqMetricsApp = addNode("rhq-metrics-app");
//        rhqMetricsApp.setProperty("name", "Metrics");
//        rhqMetricsApp.setProperty("url", "http://127.0.0.1");
//        rhqMetricsApp.setProperty("version", "1.0");

        BaseArtifactType cpu1 = addNode("CPU1");
//        cpu1.setProperty("name", "CPU1");
//        cpu1.setProperty("nodel-name", "Intel(R) Core(TM) i7-2620M CPU @ 2.70GHz");
//        cpu1.setProperty("cpu-cores", "2");

        BaseArtifactType cpu2 = addNode("CPU2");
//        cpu2.setProperty("name", "CPU1");
//        cpu2.setProperty("nodel-name", "Intel(R) Core(TM) i7-2620M CPU @ 2.70GHz");
//        cpu2.setProperty("cpu-cores", "2");

        BaseArtifactType ram = addNode("RAM");
//        ram.setProperty("name", "RAM");
//        ram.setProperty("total", "16314444 kB");

        // edges
        addEdge(wildfly1, host, "isChildOf");
        addEdge(wildfly2, host, "isChildOf");
        addEdge(lb, host, "isChildOf");
        addEdge(cpu1, host, "isChildOf");
        addEdge(cpu2, host, "isChildOf");
        addEdge(ram, host, "isChildOf");
        addEdge(db, host, "isChildOf");

        addEdge(rhqMetrics1, wildfly1, "isDeployedOn");
        addEdge(rhqMetricsDS1, wildfly1, "isDeployedOn");

        addEdge(rhqMetrics2, wildfly2, "isDeployedOn");
        addEdge(rhqMetricsDS2, wildfly2, "isDeployedOn");

        // "uses" is reserved word in artificer (org.overlord.sramp.common.error.ReservedNameException)
        addEdge(rhqMetrics1, rhqMetricsDS1, "uses2");
        addEdge(rhqMetrics2, rhqMetricsDS2, "uses2");

        addEdge(rhqMetricsDS1, db, "requires");
        addEdge(rhqMetricsDS2, db, "requires");

        addEdge(rhqMetricsApp, rhqMetrics1, "consistOf");
        addEdge(rhqMetricsApp, rhqMetrics2, "consistOf");
        addEdge(rhqMetricsApp, lb, "consistOf");
        addEdge(rhqMetricsApp, db, "consistOf");

        addEdge(rhqMetricsApp, oldDb, "consistOf");
    }

    abstract protected void run() throws Exception;

    public ArtifactService getArtifactService() {
        return artifactService;
    }

    public QueryService getQueryService() {
        return queryService;
    }
}
