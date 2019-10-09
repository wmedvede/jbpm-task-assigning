package org.jbpm.task.assigning.runtime.service;

/**
 * Keeps the definition of the solvers that must be started, managed, etc.
 */
public interface SolverDefRegistry {

    void init();

    SolverDef getSolverDef();

}
