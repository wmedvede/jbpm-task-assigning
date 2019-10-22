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

package org.jbpm.task.assigning;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jbpm.task.assigning.model.OrganizationalEntity;
import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.model.TaskOrUser;
import org.jbpm.task.assigning.model.User;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

public class BaseTaskAssigningTest {

    public static final String _24TASKS_8USERS_SOLUTION = "/data/unsolved/24tasks-8users.xml";
    public static final String _50TASKS_5USERS_SOLUTION = "/data/unsolved/50tasks-5users.xml";
    public static final String _100TASKS_5USERS_SOLUTION = "/data/unsolved/100tasks-5users.xml";
    public static final String _500TASKS_20USERS_SOLUTION = "/data/unsolved/500tasks-20users.xml";

    protected boolean writeTestFiles() {
        return Boolean.parseBoolean(System.getProperty("org.jbpm.task.assigning.test.writeFiles", "false"));
    }

    protected SolverFactory<TaskAssigningSolution> createSolverFactory() {
        SolverFactory<TaskAssigningSolution> solverFactory = SolverFactory.createEmpty();
        SolverConfig config = solverFactory.getSolverConfig();
        config.setSolutionClass(TaskAssigningSolution.class);
        config.setEntityClassList(Arrays.asList(TaskOrUser.class, Task.class));
        config.setScoreDirectorFactoryConfig(new ScoreDirectorFactoryConfig().withScoreDrls("org/jbpm/task/assigning/solver/taskAssigningScoreRules.drl"));
        return solverFactory;
    }

    protected Solver<TaskAssigningSolution> createDaemonSolver() {
        SolverFactory<TaskAssigningSolution> solverFactory = createSolverFactory();
        solverFactory.getSolverConfig().setDaemon(true);
        return solverFactory.buildSolver();
    }

    protected Solver<TaskAssigningSolution> createNonDaemonSolver(long millisecondsSpentLimit) {
        SolverFactory<TaskAssigningSolution> solverFactory = createSolverFactory();
        solverFactory.getSolverConfig().setTerminationConfig(new TerminationConfig().withMillisecondsSpentLimit(millisecondsSpentLimit));
        return solverFactory.buildSolver();
    }

    protected TaskAssigningSolution readTaskAssigningSolution(String resource) throws IOException {
        int index = resource.lastIndexOf("/");
        String prefix = resource;
        if (index >= 0) {
            prefix = resource.substring(index + 1);
        }
        File f = File.createTempFile(prefix, null);
        InputStream resourceAsStream = getClass().getResourceAsStream(resource);
        FileUtils.copyInputStreamToFile(resourceAsStream, f);
        XStreamSolutionFileIO<TaskAssigningSolution> solutionFileIO = new XStreamSolutionFileIO<>(TaskAssigningSolution.class);
        return solutionFileIO.read(f);
    }

    private static void appendln(StringBuilder builder) {
        builder.append('\n');
    }

    private static void appendln(StringBuilder builder, String text) {
        builder.append(text);
        appendln(builder);
    }

    public static void printSolution(TaskAssigningSolution solution, StringBuilder builder) {
        solution.getUserList().forEach(taskOrUser -> {
            appendln(builder, "------------------------------------------");
            appendln(builder, printUser(taskOrUser));
            appendln(builder, "------------------------------------------");
            appendln(builder);
            Task task = taskOrUser.getNextTask();
            while (task != null) {
                builder.append(" -> ");
                appendln(builder, printTask(task));
                task = task.getNextTask();
                if (task != null) {
                    appendln(builder);
                }
            }
            appendln(builder);
        });
    }

    public static String printSolution(TaskAssigningSolution solution) {
        StringBuilder builder = new StringBuilder();
        printSolution(solution, builder);
        return builder.toString();
    }

    public static String printUser(User user) {
        return "User{" +
                "id=" + user.getId() +
                ", entityId='" + user.getEntityId() + '\'' +
                ", groups=" + printOrganizationalEntities(user.getGroups()) +
                '}';
    }

    public static String printTask(Task task) {
        StringBuilder builder = new StringBuilder();
        builder.append(task.getName() +
                               ", pinned: " + task.isPinned() +
                               ", priority: " + task.getPriority() +
                               ", startTime: " + task.getStartTime() +
                               ", endTime: " + task.getEndTime() +
                               ", user: " + task.getUser().getEntityId() +
                               ", potentialOwners: " + printOrganizationalEntities(task.getPotentialOwners()));
        return builder.toString();
    }

    public static String printOrganizationalEntities(Set<? extends OrganizationalEntity> potentialOwners) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        if (potentialOwners != null) {
            potentialOwners.forEach(organizationalEntity -> {
                if (builder.length() > 1) {
                    builder.append(", ");
                }
                if (organizationalEntity.isUser()) {
                    builder.append("user = " + organizationalEntity.getEntityId());
                } else {
                    builder.append("group = " + organizationalEntity.getEntityId());
                }
            });
        }
        builder.append("}");
        return builder.toString();
    }
}