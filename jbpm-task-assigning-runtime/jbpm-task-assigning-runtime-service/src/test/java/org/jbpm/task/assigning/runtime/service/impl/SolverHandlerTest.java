package org.jbpm.task.assigning.runtime.service.impl;

import org.jbpm.task.assigning.process.runtime.integration.client.ProcessRuntimeIntegrationClient;
import org.jbpm.task.assigning.process.runtime.integration.client.ProcessRuntimeIntegrationClientFactory;
import org.jbpm.task.assigning.user.system.integration.impl.WildflyUserSystemService;
import org.junit.Ignore;
import org.junit.Test;

public class SolverHandlerTest {

    @Ignore
    @Test
    public void loadSolutionTest() {
        SolverDefRegistryImpl solverDefRegistry = new SolverDefRegistryImpl();
        solverDefRegistry.init();
        WildflyUserSystemService userSystemService = new WildflyUserSystemService();
        ProcessRuntimeIntegrationClient runtimeClient = ProcessRuntimeIntegrationClientFactory.newIntegrationClient("http://localhost:8080/kie-server/services/rest/server",
                                                                                                             "wbadmin",
                                                                                                        "wbadmin");
        SolverHandler solverHandler = new SolverHandler(solverDefRegistry.getSolverDef(),
                                                        runtimeClient,
                                                        userSystemService,
                                                        null);


        solverHandler.init();
        solverHandler.start();
        int i = 0;
    }
}
