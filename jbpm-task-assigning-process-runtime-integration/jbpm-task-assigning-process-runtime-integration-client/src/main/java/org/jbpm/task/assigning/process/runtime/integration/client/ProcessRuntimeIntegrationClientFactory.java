package org.jbpm.task.assigning.process.runtime.integration.client;

import java.util.Collections;

import org.jbpm.task.assigning.process.runtime.integration.client.impl.ProcessRuntimeIntegrationClientImpl;
import org.kie.server.api.KieServerConstants;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;

public class ProcessRuntimeIntegrationClientFactory {

    static {
        //Ensure user bypass is on to be able to e.g. let the client "admin" user to claim tasks on behalf of other
        // users
        System.setProperty("org.kie.server.bypass.auth.user", Boolean.TRUE.toString());
    }

    private static KieServicesClient createKieServicesClient(final String endpoint,
                                                             final String login,
                                                             final String password) {

        final KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(endpoint, login, password);
        configuration.setTimeout(60000);
        configuration.setCapabilities(Collections.singletonList(KieServerConstants.CAPABILITY_BPM));

        //TODO set additional configuration parameters if neccesary.
        //final String kieServerEndpoint = System.getProperty(KieServerConstants.KIE_SERVER_LOCATION);
        //configuration.setMarshallingFormat(isKieServerRendererEnabled() ? MarshallingFormat.JSON : MarshallingFormat.XSTREAM);
        //configuration.setLoadBalancer(LoadBalancer.getDefault(endpoint));
        return KieServicesFactory.newKieServicesClient(configuration);
    }

    public static ProcessRuntimeIntegrationClient newIntegrationClient(final String endpoint,
                                                                       final String login,
                                                                       final String password) {
        KieServicesClient servicesClient = createKieServicesClient(endpoint, login, password);
        UserTaskServicesClient userTaskServicesClient = servicesClient.getServicesClient(UserTaskServicesClient.class);
        QueryServicesClient queryServicesClient = servicesClient.getServicesClient(QueryServicesClient.class);
        return new ProcessRuntimeIntegrationClientImpl(userTaskServicesClient, queryServicesClient);
    }

    //TODO remove this method.
    public static KieServicesClient newKieServicesClient(final String endpoint,
                                                         final String login,
                                                         final String password) {
        return createKieServicesClient(endpoint, login, password);
    }
}
