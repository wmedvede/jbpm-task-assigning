package org.jbpm.task.assigning.runtime.service;

import java.util.List;

import org.optaplanner.core.impl.solver.ProblemFactChange;

public interface TaskAssigningService {

    /**
     * 1) find the servers
     * 2) find the solver configurations that must be started
     * 3) start the solvers.
     */
    void init();

    /**
     * stop the running solvers.
     */
    void destroy();

    void addProblemFactChanges(List<ProblemFactChange<TaskAssigningService>> changes);
}
