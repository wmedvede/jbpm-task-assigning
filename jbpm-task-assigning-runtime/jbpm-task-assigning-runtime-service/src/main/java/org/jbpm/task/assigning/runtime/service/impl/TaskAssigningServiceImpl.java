package org.jbpm.task.assigning.runtime.service.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.jbpm.task.assigning.process.runtime.integration.client.ProcessRuntimeIntegrationClient;
import org.jbpm.task.assigning.runtime.service.SolverDef;
import org.jbpm.task.assigning.runtime.service.SolverDefRegistry;
import org.jbpm.task.assigning.runtime.service.TaskAssigningService;
import org.jbpm.task.assigning.user.system.integration.UserSystemService;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskAssigningServiceImpl implements TaskAssigningService {

    private static Logger LOGGER = LoggerFactory.getLogger(TaskAssigningServiceImpl.class);

    private SolverDefRegistry solverDefRegistry;
    private ProcessRuntimeIntegrationClient runtimeClient;
    private UserSystemService userSystemService;
    private ExecutorService executorService;

    private SolverHandler solverHandler;

    public TaskAssigningServiceImpl(SolverDefRegistry solverDefRegistry,
                                    ProcessRuntimeIntegrationClient runtimeClient,
                                    UserSystemService userSystemService,
                                    ExecutorService executorService) {
        this.solverDefRegistry = solverDefRegistry;
        this.runtimeClient = runtimeClient;
        this.userSystemService = userSystemService;
        this.executorService = executorService;
        solverDefRegistry.init();
    }

    @Override
    public void init() {
        SolverDef solverDef = solverDefRegistry.getSolverDef();
        solverHandler = new SolverHandler(solverDef, runtimeClient, userSystemService);
        executorService.execute(() -> {
            solverHandler.init();
            solverHandler.start();
        });
    }

    @Override
    public void addProblemFactChanges(List<ProblemFactChange<TaskAssigningService>> changes) {
        synchronized (solverHandler) {
            //solverHandler
        }

    }

    @Override
    public void destroy() {
        solverHandler.destroy();
    }
}
