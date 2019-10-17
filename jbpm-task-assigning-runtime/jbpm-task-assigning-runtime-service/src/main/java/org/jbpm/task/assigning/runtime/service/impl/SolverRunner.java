package org.jbpm.task.assigning.runtime.service.impl;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

/**
 * This class is intended to manage the solver life-cycle in a multi-threaded environment.
 */
public class SolverRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolverRunner.class);

    private final Solver<TaskAssigningSolution> solver;
    private TaskAssigningSolution solution;

    private final AtomicBoolean starting = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean destroyed = new AtomicBoolean(false);
    private final Semaphore startPermit = new Semaphore(0);

    public SolverRunner(final Solver<TaskAssigningSolution> solver,
                        final SolverEventListener<TaskAssigningSolution> eventListener) {
        checkNotNull("solver", solver);
        checkNotNull("eventListener", eventListener);
        this.solver = solver;
        this.solver.addEventListener((event) -> {
            if (!isDestroyed()) {
                eventListener.bestSolutionChanged(event);
            }
        });
    }

    /**
     * This method is invoked from a different thread for starting the solver and must be invoked only one time.
     * If the solver was already started an exception is thrown so callers of this method should call the isStarted()
     * method to verify. This method is not thread-safe so it's expected that any synchronization required between the
     * isStarted() and start() methods is performed by the callers. However it's normally not expected that multiple
     * threads might try to start the same solver runner instance.
     * @param solution a valid solution for starting the solver with.
     */
    public void start(final TaskAssigningSolution solution) {
        if (starting.getAndSet(true)) {
            throw new RuntimeException("SolverRunner start method was already invoked.");
        }
        this.solution = solution;
        startPermit.release();
    }

    /**
     * @return true if the solver has been started, false in any other case.
     */
    public boolean isStarted() {
        return started.get();
    }

    /**
     * This method programmes the subsequent finalization of the solver (if it was started) that will be produced as
     * soon as possible by invoking the solver.terminateEarly() method. If the solver wasn't started it just finalizes
     * current thread.
     */
    public void destroy() {
        destroyed.set(true);
        if (isStarted()) {
            solver.terminateEarly();
        } else {
            startPermit.release();
        }
    }

    /**
     * @return true if the solver has been destroyed, false in any other case.
     */
    public boolean isDestroyed() {
        return destroyed.get();
    }

    /**
     * Thread-safe method that adds a list of ProblemFactChanges to the current solver.
     * If the solver has not been started an exception is thrown, so it's expected that callers of this method should
     * call isStarted() method to verify.
     * @param changes a list of problem fact changes to program in current solver.
     */
    public void addProblemFactChanges(final List<ProblemFactChange<TaskAssigningSolution>> changes) {
        if (isStarted() && !isDestroyed()) {
            solver.addProblemFactChanges(changes);
        } else {
            throw new RuntimeException("SolverRunner wasn't yet started");
        }
    }

    @Override
    public void run() {
        try {
            LOGGER.debug("SolverRunner is waiting for a solution to start with.");
            startPermit.acquire();
            LOGGER.debug("SolverRunner will be started.");
            if (!isDestroyed()) {
                started.set(true);
                solver.solve(solution);
            }
        } catch (InterruptedException e) {
            LOGGER.debug("SolverWorked was interrupted.", e);
        }
    }
}