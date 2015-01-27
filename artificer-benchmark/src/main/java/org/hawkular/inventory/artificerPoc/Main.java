package org.hawkular.inventory.artificerPoc;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.overlord.sramp.server.core.api.ArtifactService;
import org.overlord.sramp.server.core.api.QueryService;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Properties;

/**
 * Created by jkremser on 01/27/15.
 */
public class Main {
    public static void main(String[] args) {
        ExtendedArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setExtendedType("FooArtifactType");
        artifact.setName("Foo");
        artifact.setDescription("I'm a Foo");

        try {
            Properties jndiProps = new Properties();
            jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
            jndiProps.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
            jndiProps.put("jboss.naming.client.ejb.context", true);
            Context context = new InitialContext(jndiProps);

            final ArtifactService artifactService = (ArtifactService) context.lookup(
                    "s-ramp-server/ArtifactService!" + ArtifactService.class.getName());
            artifactService.login("artificer", "artificer1!");
            artifactService.create(artifact);

            final QueryService queryService = (QueryService) context.lookup(
                    "s-ramp-server/QueryService!" + QueryService.class.getName());
            queryService.login("artificer", "artificer1!");
            List<BaseArtifactType> artifactTypes = queryService.query("/s-ramp/ext/FooArtifactType");
            for (BaseArtifactType type : artifactTypes) {
                System.out.println(type.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
