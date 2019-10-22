/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.task.assigning.runtime.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.process.runtime.integration.client.ProcessRuntimeIntegrationClient;
import org.jbpm.task.assigning.process.runtime.integration.client.TaskInfo;
import org.jbpm.task.assigning.user.system.integration.UserSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.InProgress;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Ready;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Reserved;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Suspended;
import static org.kie.soup.commons.validation.PortablePreconditions.checkCondition;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

/**
 * This class manages reading of current jBPM state and offer the results to the consumer for updating current solution
 * with the potential changes. Additionally at the first time, when the SolverExecutor is not yet started, it manages
 * the initial solution recovery from the proper repository and invokes the SolverExecutor start.
 * As soon the SolverExecutor was started it starts the synchronization with the configured period by implementing a
 * polling strategy.
 */
public class SolutionSynchronizer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolutionSynchronizer.class);

    private final SolverExecutor solverExecutor;
    private final PublishedTaskCache publishedTasks;
    private final ProcessRuntimeIntegrationClient runtimeClient;
    private final UserSystemService userSystemService;
    private final long period;
    private final Consumer<List<TaskInfo>> taskInfoConsumer;

    private final Semaphore startPermit = new Semaphore(0);
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    public SolutionSynchronizer(final SolverExecutor solverExecutor,
                                final PublishedTaskCache publishedTasks,
                                final ProcessRuntimeIntegrationClient runtimeClient,
                                final UserSystemService userSystemService,
                                final long period,
                                final Consumer<List<TaskInfo>> taskInfoConsumer) {
        checkNotNull("solverExecutor", solverExecutor);
        checkNotNull("publishedTasks", publishedTasks);
        checkNotNull("runtimeClient", runtimeClient);
        checkNotNull("taskInfoConsumer", taskInfoConsumer);
        checkCondition("period", period > 5);
        this.solverExecutor = solverExecutor;
        this.publishedTasks = publishedTasks;
        this.runtimeClient = runtimeClient;
        this.userSystemService = userSystemService;
        this.period = period;
        this.taskInfoConsumer = taskInfoConsumer;
    }

    /**
     * This method starts the SolutionSynchronizer. It's a non thread-safe method, but only the first invocation
     * has effect.
     */
    public void start() {
        startPermit.release();
    }

    /**
     * This method programmes the subsequent finalization of the processing, that will be produced as soon as possible.
     * It's a non thread-safe method, but only first invocation has effect.
     */
    public void destroy() {
        destroyed.set(true);
        startPermit.release(); //in case it's still waiting for start.
    }

    @Override
    public void run() {
        LOGGER.debug("Solution Synchronizer Started");
        try {
            //wait until the start() method is invoked at any point of time.
            startPermit.acquire();
        } catch (InterruptedException e) {
            LOGGER.error("Solution Synchronizer was interrupted while waiting for start.", e);
        }
        while (notExit()) {
            try {
                Thread.sleep(period);
                if (notExit()) {
                    if (!solverExecutor.isStarted()) {
                        try {
                            LOGGER.debug("Solution Synchronizer loading initial solution.");
                            final TaskAssigningSolution recoveredSolution = recoverSolution();
                            if (notExit() && !solverExecutor.isDestroyed()) {
                                if (!recoveredSolution.getTaskList().isEmpty()) {
                                    solverExecutor.start(recoveredSolution);
                                    LOGGER.debug("Initial solution was successfully loaded.");
                                } else {
                                    LOGGER.debug("It looks like there are no tasks for loading an initial solution at this moment. " +
                                                         "Next attempt will be in " + period + " milliseconds");
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error("An error was produced during initial solution loading. " +
                                                 "Next attempt will be in " + period + " milliseconds", e);
                        }
                    } else {
                        try {
                            LOGGER.debug("Refreshing solution status from external repository.");
                            final List<TaskInfo> updatedTaskInfos = loadTaskInfos();
                            LOGGER.debug("Status was read successful.");
                            if (notExit()) {
                                taskInfoConsumer.accept(updatedTaskInfos);
                            }
                        } catch (Exception e) {
                            LOGGER.error("An error was produced during solution status refresh from external repository" +
                                                 "Next attempt will be in " + period + " milliseconds", e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.error("Solution Synchronizer was interrupted.", e);
            }
        }
        LOGGER.debug("Solution Synchronizer finished");
    }

    private boolean notExit() {
        return !destroyed.get() && !Thread.currentThread().isInterrupted();
    }

    private TaskAssigningSolution recoverSolution() {
        final List<TaskInfo> taskInfos = runtimeClient.findTasks(Arrays.asList(Ready, Reserved, InProgress, Suspended),
                                                                 0,
                                                                 100000);
        final List<org.jbpm.task.assigning.user.system.integration.User> externalUsers = userSystemService.findAllUsers();
        return new SolutionBuilder()
                .withTasks(taskInfos)
                .withUsers(externalUsers)
                .withCache(publishedTasks)
                .build();
    }

    private List<TaskInfo> loadTaskInfos() {
        return runtimeClient.findTasks(Arrays.asList(Ready, Reserved, InProgress, Suspended), 0, 100000);
    }
}
