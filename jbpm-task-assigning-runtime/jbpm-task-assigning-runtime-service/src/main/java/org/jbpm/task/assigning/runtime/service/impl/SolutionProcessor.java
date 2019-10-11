package org.jbpm.task.assigning.runtime.service.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.process.runtime.integration.client.ProcessRuntimeIntegrationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolutionProcessor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolutionProcessor.class);

    private BlockingQueue<TaskAssigningSolution> queue;
    private ProcessRuntimeIntegrationClient runtimeClient;
    private AtomicBoolean destroyed = new AtomicBoolean(false);

    public SolutionProcessor(BlockingQueue<TaskAssigningSolution> queue, ProcessRuntimeIntegrationClient runtimeClient) {
        this.queue = queue;
        this.runtimeClient = runtimeClient;
    }

    @Override
    public void run() {
        while (!destroyed.get()) {
            try {
                final TaskAssigningSolution solution = queue.take();
                processSolution(solution);
            } catch (InterruptedException e) {
                LOGGER.error("An error was produced during solution dequeuing", e);
            }
        }
    }

    public void destroy() {
        destroyed.set(true);
    }

    private void processSolution(final TaskAssigningSolution solution) {

    }
}
