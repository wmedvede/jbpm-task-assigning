package org.jbpm.task.assigning.runtime.service.impl;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.process.runtime.integration.client.ProcessRuntimeIntegrationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

/**
 * This class manges the processing of new a solution produced by the solver. It must typically apply all the required
 * changes in the jBPM runtime.
 */
public class SolutionProcessor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolutionProcessor.class);

    private final ProcessRuntimeIntegrationClient runtimeClient;
    private final Consumer<Result> resultConsumer;

    private final Semaphore solutionResource = new Semaphore(0);
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    private TaskAssigningSolution solution;

    public static class Result {

        private Exception error;

        public Result() {
        }

        private Result(Exception error) {
            this.error = error;
        }

        public boolean hasError() {
            return error != null;
        }

        public Exception getError() {
            return error;
        }
    }

    public SolutionProcessor(final ProcessRuntimeIntegrationClient runtimeClient, final Consumer<Result> resultConsumer) {
        checkNotNull("runtimeClient", runtimeClient);
        checkNotNull("resultConsumer", resultConsumer);
        this.runtimeClient = runtimeClient;
        this.resultConsumer = resultConsumer;
    }

    /**
     * @return true if a solution is being processed at this time, false in any other case.
     */
    public boolean isProcessing() {
        return processing.get();
    }

    /**
     * This method is invoked form a different thread for doing the processing of a solution. This method is not
     * thread-safe and it's expected that any synchronization required between the isProcessing() and process()
     * methods is performed by the caller. Since only one solution can be processed at time, the caller should typically
     * execute in the following sequence.
     * if (!solutionProcessor.isProcessing()) {
     * solutionProcessor.process(solution);
     * } else {
     * //save/discard the solution and/or invoke at a later time.
     * }
     * A null value will throw an exception.
     * @param solution a solution to process.
     */
    public void process(final TaskAssigningSolution solution) {
        checkNotNull("solution", solution);
        processing.set(true);
        this.solution = solution;
        solutionResource.release();
    }

    public void destroy() {
        destroyed.set(true);
        solutionResource.release(); //in case it was waiting for a solution to process.
    }

    @Override
    public void run() {
        while (!destroyed.get() && !Thread.currentThread().isInterrupted()) {
            try {
                solutionResource.acquire();
                if (!destroyed.get()) {
                    process(solution);
                }
            } catch (InterruptedException e) {
                LOGGER.error("Solution Processor was interrupted", e);
            }
        }
    }

    private void doProcess(final TaskAssigningSolution solution) {
        LOGGER.debug("Starting processing of solution: " + solution);
        LOGGER.debug("Solution processing finished: " + solution);
    }
}
