package org.jbpm.task.assigning.runtime.service.impl;

import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.runtime.service.SolverDef;
import org.jbpm.task.assigning.runtime.service.SolverDefRegistry;
import org.jbpm.task.assigning.runtime.service.TaskAssigningService;
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
