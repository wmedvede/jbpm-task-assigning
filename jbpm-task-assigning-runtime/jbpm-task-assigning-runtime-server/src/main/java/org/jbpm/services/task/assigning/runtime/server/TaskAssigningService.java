package org.jbpm.services.task.assigning.runtime.server;

public interface TaskAssigningService {

    /**
     * 1) find the servers
     * 2) find the solver configurations that must be started
     * 3) start the solvers.
     */
    void start();

    /**
     * stop the running solvers.
     */
    void destroy();

}
