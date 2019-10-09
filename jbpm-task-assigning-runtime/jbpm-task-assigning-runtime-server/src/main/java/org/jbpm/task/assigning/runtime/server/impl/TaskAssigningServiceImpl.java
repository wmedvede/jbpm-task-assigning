package org.jbpm.task.assigning.runtime.server.impl;

import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.runtime.server.SolverDef;
import org.jbpm.task.assigning.runtime.server.SolverDefRegistry;
import org.jbpm.task.assigning.runtime.server.TaskAssigningService;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskAssigningServiceImpl implements TaskAssigningService {

    private static Logger LOGGER = LoggerFactory.getLogger(TaskAssigningServiceImpl.class);

    private SolverDefRegistry solverDefRegistry;

    public TaskAssigningServiceImpl(SolverDefRegistry solverDefRegistry) {
        this.solverDefRegistry = solverDefRegistry;
        solverDefRegistry.init();
    }

    @Override
    public void start() {
        SolverDef solverDef = solverDefRegistry.getSolverDef();
        SolverFactory<TaskAssigningSolution> solverFactory = SolverFactory.createFromXmlResource(solverDef.getSolverConfigFile());
        Solver<TaskAssigningSolution> solver = solverFactory.buildSolver();
        solver.addProblemFactChange(null);

    }

    @Override
    public void destroy() {

    }


}
