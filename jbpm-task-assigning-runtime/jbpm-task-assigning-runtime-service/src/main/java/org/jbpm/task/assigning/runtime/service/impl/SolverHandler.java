package org.jbpm.task.assigning.runtime.service.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.process.runtime.integration.client.ProcessRuntimeIntegrationClient;
import org.jbpm.task.assigning.process.runtime.integration.client.TaskInfo;
import org.jbpm.task.assigning.runtime.service.SolverDef;
import org.jbpm.task.assigning.user.system.integration.UserSystemService;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

/**
 * This class handles all the work regarding with: creating/starting the solver, the processing of the produced solution,
 * and the synchronization of the working solution with the changes that might be produced in the jBPM runtime. By
 * coordinating the actions produced by the SolverRunner, the SolutionProcessor and the SolutionSynchronizer.
 */
public class SolverHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(SolverHandler.class);

    enum STATUS {
        /**
         * SolverHandler was created but the Solver was not yet created/started.
         */
        CREATED,
        /**
         * The Solver was created, but not yet started.
         */
        INITIALIZED,
        STARTED,
        DESTROYED
    }

    private final SolverDef solverDef;
    private final ProcessRuntimeIntegrationClient runtimeClient;
    private final UserSystemService userSystemService;
    private final ExecutorService executorService;

    /**
     * Synchronizes potential concurrent accesses by the SolverWorker, SolutionProcessor and SolutionSynchronizer.
     */
    private final ReentrantLock lock = new ReentrantLock();
    private TaskAssigningSolution currentSolution = null;
    private TaskAssigningSolution nextSolution = null;

    private Solver<TaskAssigningSolution> solver;
    private SolverRunner solverRunner;
    private SolutionSynchronizer solutionSynchronizer;
    private SolutionProcessor solutionProcessor;

    private STATUS status = STATUS.CREATED;

    public SolverHandler(final SolverDef solverDef,
                         final ProcessRuntimeIntegrationClient runtimeClient,
                         final UserSystemService userSystemService,
                         final ExecutorService executorService) {
        checkNotNull("solverDef", solverDef);
        checkNotNull("runtimeClient", runtimeClient);
        checkNotNull("userSystemService", userSystemService);
        checkNotNull("executorService", executorService);
        this.solverDef = solverDef;
        this.runtimeClient = runtimeClient;
        this.userSystemService = userSystemService;
        this.executorService = executorService;
    }

    public void init() {
        solver = createSolver(solverDef);
        status = STATUS.INITIALIZED;
    }

    public void start() {
        solverRunner = new SolverRunner(solver, this::onBestSolutionChange);
        solutionSynchronizer = new SolutionSynchronizer(solverRunner, runtimeClient, userSystemService, 10000,
                                                        this::onSynchronizeSolution);
        solutionProcessor = new SolutionProcessor(runtimeClient, this::onSolutionProcessed);
        executorService.execute(solverRunner); //is started by the solutionSynchronizer
        executorService.execute(solutionSynchronizer);
        executorService.execute(solutionProcessor); //automatically starts and waits for a solution to process.
        solutionSynchronizer.start();

        status = STATUS.STARTED;
    }

    public void destroy() {
        this.status = STATUS.DESTROYED;
        solverRunner.destroy();
        solutionSynchronizer.destroy();
        solutionProcessor.destroy();

        executorService.shutdown();
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.debug("An exception was thrown during executionService graceful termination.", e);
            executorService.shutdownNow();
        }
    }

    public void addProblemFactChanges(List<ProblemFactChange<TaskAssigningSolution>> changes) {
        checkNotNull("changes", changes);
        if (!solverRunner.isStarted()) {
            LOGGER.debug("SolverRunner has not yet been started. Changes will be discarded", changes);
        }
        if (solverRunner.isDestroyed()) {
            LOGGER.debug("SolverRunner has been destroyed. Changes will be discarded", changes);
        }
        solverRunner.addProblemFactChanges(changes);
    }

    private void onBestSolutionChange(BestSolutionChangedEvent<TaskAssigningSolution> event) {
        if (event.isEveryProblemFactChangeProcessed() && event.getNewBestSolution().getScore().isSolutionInitialized()) {
            lock.lock();
            try {
                if (solutionProcessor.isProcessing()) {
                    nextSolution = event.getNewBestSolution();
                } else {
                    currentSolution = event.getNewBestSolution();
                    nextSolution = null;
                    solutionProcessor.process(currentSolution);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void onSolutionProcessed(SolutionProcessor.Result result) {
        LOGGER.debug("Solution was processed with result: " + result.hasError());
        lock.lock();
        try {
            if (nextSolution != null) {
                currentSolution = nextSolution;
                solutionProcessor.process(currentSolution);
            }
        } finally {
            lock.unlock();
        }
    }

    private void onSynchronizeSolution(List<TaskInfo> taskInfos) {
        // 1) iterate the tasks and program the proper problem fact changes.

    }

    private Solver<TaskAssigningSolution> createSolver(SolverDef solverDef) {
        SolverFactory<TaskAssigningSolution> solverFactory = SolverFactory.createFromXmlResource(solverDef.getSolverConfigFile());
        return solverFactory.buildSolver();
    }
}
