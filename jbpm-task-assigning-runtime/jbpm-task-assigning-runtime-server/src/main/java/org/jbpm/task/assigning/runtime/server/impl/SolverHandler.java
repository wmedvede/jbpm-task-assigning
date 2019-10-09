package org.jbpm.task.assigning.runtime.server.impl;

import java.util.Arrays;
import java.util.List;

import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.runtime.server.SolverDef;
import org.jbpm.task.assigning.process.runtime.integration.client.ProcessRuntimeIntegrationClient;
import org.jbpm.task.assigning.process.runtime.integration.client.TaskInfo;
import org.jbpm.task.assigning.user.system.integration.UserSystemService;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;

import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.InProgress;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Ready;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Reserved;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Suspended;

public class SolverHandler {

    enum STATUS {
        CREATED,
        INITIALIZED,
    }

    private SolverDef solverDef;
    private Solver<TaskAssigningSolution> solver;
    private ProcessRuntimeIntegrationClient runtimeClient;
    private UserSystemService userSystemService;

    private STATUS status = STATUS.CREATED;

    private TaskAssigningSolution currentSolution;

    public void init() {
        solver = createSolver(solverDef);
        solver.addEventListener(this::onBestSolutionChange);
        loadSolution();
    }

    //TODO to be continued here
    private TaskAssigningSolution loadSolution() {
        final List<TaskInfo> taskInfos = runtimeClient.findTasks(Arrays.asList(Ready, Reserved, InProgress, Suspended), 0, 10000);
        final List<org.jbpm.task.assigning.user.system.integration.User> externalUsers = userSystemService.findAllUsers();
        return new SolutionBuilder()
                .withTasks(taskInfos)
                .withUsers(externalUsers)
                .build();
    }

    private void onBestSolutionChange(BestSolutionChangedEvent<TaskAssigningSolution> event) {

    }

    private Solver<TaskAssigningSolution> createSolver(SolverDef solverDef) {
        SolverFactory<TaskAssigningSolution> solverFactory = SolverFactory.createFromXmlResource(solverDef.getSolverConfigFile());
        return solverFactory.buildSolver();
    }
}
