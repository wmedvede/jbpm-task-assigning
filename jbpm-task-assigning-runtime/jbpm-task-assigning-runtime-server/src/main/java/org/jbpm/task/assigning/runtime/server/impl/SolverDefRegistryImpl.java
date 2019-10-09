package org.jbpm.task.assigning.runtime.server.impl;

import org.jbpm.task.assigning.runtime.server.SolverDef;
import org.jbpm.task.assigning.runtime.server.SolverDefRegistry;

public class SolverDefRegistryImpl implements SolverDefRegistry {

    private static final String TASK_ASSIGNING_SOLVER_ID = "jbpm-task-assigning-solver-id";
    private static final String TASK_ASSIGNING_SOLVER_CONFIG = "org.jbpm.services.task.assigning.solverConfig";

    private SolverDef solverDef;

    @Override
    public void init() {
        String solverConfig = System.getProperty(TASK_ASSIGNING_SOLVER_CONFIG, "taskAssigningSolverConfig.xml");
        solverDef = new SolverDef(TASK_ASSIGNING_SOLVER_ID, solverConfig);
    }

    @Override
    public SolverDef getSolverDef() {
        return solverDef;
    }
}
