package org.jbpm.services.task.assigning.runtime.server;

/**
 * Keeps the definition of the solvers that must be started, managed, etc.
 */
public interface SolverDefRegistry {

    void init();

    SolverDef getSolverDef();

}
