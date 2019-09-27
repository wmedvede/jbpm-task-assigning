package org.jbpm.services.task.assigning.model.solver.realtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.jbpm.services.task.assigning.model.Task;
import org.jbpm.services.task.assigning.model.TaskAssigningSolution;
import org.jbpm.services.task.assigning.model.User;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AddTaskProblemFactChangeTest extends BaseProblemFactChangeTest {

    @Test
    public void addTaskProblemFactChange24Tasks5UsersTest() throws Exception {
        addTaskProblemFactChangeTest(_24TASKS_8USERS_SOLUTION, Arrays.asList(24L, 25L, 30L, 40L));
    }

    @Test
    public void addTaskProblemFactChange24Tasks5UsersRandomTest() throws Exception {
        addTaskProblemFactChangeRandomSetTest(_24TASKS_8USERS_SOLUTION);
    }

    @Test
    public void addTaskProblemFactChange50Tasks5UsersTest() throws Exception {
        addTaskProblemFactChangeTest(_50TASKS_5USERS_SOLUTION, Arrays.asList(50L, 520L, 70L, 85L, 100L));
    }

    @Test
    public void addTaskProblemFactChange50Tasks5UsersRandomTest() throws Exception {
        addTaskProblemFactChangeRandomSetTest(_50TASKS_5USERS_SOLUTION);
    }

    @Test
    public void addTaskProblemFactChange100Tasks5UsersTest() throws Exception {
        addTaskProblemFactChangeTest(_100TASKS_5USERS_SOLUTION, Arrays.asList(100L, 105L, 200L, 350L));
    }

    @Test
    public void addTaskProblemFactChange100Tasks5UsersRandomTest() throws Exception {
        addTaskProblemFactChangeRandomSetTest(_100TASKS_5USERS_SOLUTION);
    }

    @Test
    public void addTaskProblemFactChange500Tasks5UsersTest() throws Exception {
        addTaskProblemFactChangeTest(_500TASKS_20USERS_SOLUTION, Arrays.asList(500L, 600L, 700L));
    }

    @Test
    public void addTaskProblemFactChange500Tasks5UsersRandomTest() throws Exception {
        addTaskProblemFactChangeRandomSetTest(_500TASKS_20USERS_SOLUTION);
    }

    @Test
    public void addTaskProblemFactChangeTaskAlreadyExistsTest() throws Exception {
        TaskAssigningSolution solution = readTaskAssigningSolution(_24TASKS_8USERS_SOLUTION);
        long taskId = new Random().nextInt(solution.getTaskList().size());
        Task task = new Task(taskId, null, 1);
        expectedException.expectMessage("A task with the given identifier id: " + taskId + " already exists");
        executeSequentialChanges(solution, Collections.singletonList(new ProgrammedProblemFactChange<>(new AddTaskProblemFactChange(task))));
    }

    private void addTaskProblemFactChangeRandomSetTest(String solutionResource) throws Exception {
        TaskAssigningSolution solution = readTaskAssigningSolution(solutionResource);
        int taskCount = solution.getTaskList().size();
        int randomChanges = taskCount / 2 + random.nextInt(taskCount / 2);
        List<Long> taskIds = new ArrayList<>();
        for (int i = 0; i < randomChanges; i++) {
            taskIds.add((long) taskCount++);
        }
        addTaskProblemFactChangeTest(solution, taskIds);
    }

    private void addTaskProblemFactChangeTest(TaskAssigningSolution solution, List<Long> taskIds) throws Exception {
        solution.getUserList().add(User.PLANNING_USER);
        List<ProgrammedProblemFactChange<AddTaskProblemFactChange>> programmedChanges = taskIds.stream()
                .map(id -> new ProgrammedProblemFactChange<>(new AddTaskProblemFactChange(new Task(id, "NewTask_" + id, 1))))
                .collect(Collectors.toList());

        //each partial solution must have the change that was applied on it.
        executeSequentialChanges(solution, programmedChanges);
        programmedChanges.forEach(change -> assertAddTaskProblemFactChangeWasProduced(change.getChange(), change.getSolutionAfterChange()));

        //finally the last solution must have the result of all the changes.
        TaskAssigningSolution lastSolution = programmedChanges.get(programmedChanges.size() - 1).getSolutionAfterChange();
        programmedChanges.forEach(change -> assertAddTaskProblemFactChangeWasProduced(change.getChange(), lastSolution));
    }

    private void addTaskProblemFactChangeTest(String solutionResource, List<Long> taskIds) throws Exception {
        addTaskProblemFactChangeTest(readTaskAssigningSolution(solutionResource), taskIds);
    }

    private void assertAddTaskProblemFactChangeWasProduced(AddTaskProblemFactChange change, TaskAssigningSolution solution) {
        assertTrue(solution.getTaskList().stream().anyMatch(task -> Objects.equals(change.getTask().getId(), task.getId())));
    }
}