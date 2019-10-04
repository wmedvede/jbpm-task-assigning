package org.jbpm.services.task.assigning.runtime.server.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jbpm.services.task.assigning.model.Group;
import org.jbpm.services.task.assigning.model.Task;
import org.jbpm.services.task.assigning.model.TaskAssigningSolution;
import org.jbpm.services.task.assigning.model.User;
import org.jbpm.services.task.assigning.runtime.server.SolverDef;
import org.jbpm.task.assigning.process.runtime.integration.client.PlanningParameters;
import org.jbpm.task.assigning.process.runtime.integration.client.ProcessRuntimeIntegrationClient;
import org.jbpm.task.assigning.process.runtime.integration.client.TaskInfo;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;

import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.InProgress;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Ready;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Reserved;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Suspended;

public class SolverHandler {

    enum STATUS {
        CREATED,
        INITIALIZED,
    }

    private SolverDef solverDef;
    private Solver<TaskAssigningSolution> solver;
    private ProcessRuntimeIntegrationClient runtimeClient;

    private STATUS status = STATUS.CREATED;

    private TaskAssigningSolution currentSolution;

    public void init() {
        solver = createSolver(solverDef);
        solver.addEventListener(this::onBestSolutionChange);
        loadSolution();
    }

    private TaskAssigningSolution loadSolution() {

        final List<TaskInfo> taskInfos = runtimeClient.findTasks(Arrays.asList(Ready, Reserved, InProgress, Suspended), 0, 10000);
        final List<Task> unAssignedTasks = new ArrayList<>();
        final Map<String, List<AssignedTask>> assignedTasksByUser = new HashMap<>();

        taskInfos.forEach(taskInfo -> {
            final Task task = from(taskInfo);
            if (Ready == taskInfo.getStatus()) {
                //ready tasks are assigned to nobody.
                unAssignedTasks.add(task);
            } else if (Reserved == taskInfo.getStatus() || InProgress == taskInfo.getStatus() || Suspended == taskInfo.getStatus()) {
                if (StringUtils.isNoneEmpty(taskInfo.getActualOwner())) {
                    // If actualOwner is empty the only chance is that the task was in Ready status and changed to
                    // Suspended, since Reserved and InProgress tasks has always an owner.
                    // Finally tasks with no actualOwner are skipped, since they'll be properly added to the
                    // solution when they change to Ready status and the proper jBPM event is raised.

                    final PlanningParameters currentParameters = taskInfo.getPlanningParameters();
                    boolean published;
                    boolean pinned;
                    if (currentParameters != null) {
                        //the task was already planned.
                        published = InProgress == taskInfo.getStatus() || currentParameters.isPublished();
                        pinned = published || currentParameters.isPinned();
                        if (Objects.equals(currentParameters.getAssignedUser(), taskInfo.getActualOwner())) {
                            //preserve currentParameters.
                            addTaskToUser(assignedTasksByUser, task, currentParameters.getAssignedUser(), currentParameters.getIndex(), published, pinned);
                        } else {
                            addTaskToUser(assignedTasksByUser, task, taskInfo.getActualOwner(), -1, published, pinned);
                        }
                    } else {
                        published = InProgress == taskInfo.getStatus();
                        pinned = published;
                        addTaskToUser(assignedTasksByUser, task, taskInfo.getActualOwner(), -1, published, pinned);
                    }
                }
            }
        });

        return null;
    }

    public static class AssignedTask {

        Task task;
        private int index;
        private boolean published;
        private boolean pinned;

        public AssignedTask(Task task, int index, boolean published, boolean pinned) {
            this.task = task;
            this.index = index;
            this.published = published;
            this.pinned = pinned;
        }

        public Task getTask() {
            return task;
        }

        public int getIndex() {
            return index;
        }

        public boolean isPublished() {
            return published;
        }

        public boolean isPinned() {
            return pinned;
        }
    }

    private static void addTaskToUser(Map<String, List<AssignedTask>> tasksByUser,
                                      Task task,
                                      String actualOwner,
                                      int index,
                                      boolean published,
                                      boolean pinned) {
        final List<AssignedTask> userAssignedTasks = tasksByUser.computeIfAbsent(actualOwner, key -> new ArrayList<>());
        addInOrder(userAssignedTasks, task, index, published, pinned);
    }

    public static void addInOrder(List<AssignedTask> assignedTasks,
                                  Task task,
                                  int index,
                                  boolean published,
                                  boolean pinned) {
        int insertIndex = 0;
        AssignedTask currentTask;
        final Iterator<AssignedTask> it = assignedTasks.iterator();
        boolean found = false;
        while (!found && it.hasNext()) {
            currentTask = it.next();
            if (pinned && currentTask.isPinned()) {
                found = (index >= 0) && (currentTask.getIndex() < 0 || index < currentTask.getIndex());
            } else if (pinned && !currentTask.isPinned()) {
                found = true;
            } else if (!pinned && !currentTask.isPinned()) {
                found = (index >= 0) && (currentTask.getIndex() < 0 || index < currentTask.getIndex());
            }
            insertIndex = !found ? insertIndex + 1 : insertIndex;
        }
        assignedTasks.add(insertIndex, new AssignedTask(task, index, published, pinned));
    }

    private void onBestSolutionChange(BestSolutionChangedEvent<TaskAssigningSolution> event) {

    }

    private Solver<TaskAssigningSolution> createSolver(SolverDef solverDef) {
        SolverFactory<TaskAssigningSolution> solverFactory = SolverFactory.createFromXmlResource(solverDef.getSolverConfigFile());
        return solverFactory.buildSolver();
    }

    private Task from(TaskInfo taskInfo) {
        Task task = new Task(taskInfo.getTaskId(),
                             taskInfo.getProcessInstanceId(),
                             taskInfo.getProcessId(),
                             taskInfo.getContainerId(),
                             taskInfo.getName(),
                             taskInfo.getPriority(),
                             taskInfo.getInputData());
        if (taskInfo.getPotentialOwners() != null) {
            taskInfo.getPotentialOwners().forEach(potentialOwner -> {
                if (potentialOwner.isUser()) {
                    task.getPotentialOwners().add(new User(potentialOwner.getEntityId().hashCode(), potentialOwner.getEntityId()));
                } else {
                    task.getPotentialOwners().add(new Group(potentialOwner.getEntityId().hashCode(), potentialOwner.getEntityId()));
                }
            });
        }
        return task;
    }
}
