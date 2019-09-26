package org.jbpm.services.task.assigning.model.solver;

import java.util.List;

import org.jbpm.services.task.assigning.BaseTaskAssigningTest;
import org.jbpm.services.task.assigning.model.Task;
import org.jbpm.services.task.assigning.model.TaskAssigningSolution;
import org.jbpm.services.task.assigning.model.User;
import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;

import static org.jbpm.services.task.assigning.model.solver.TaskHelper.extractTaskList;
import static org.jbpm.services.task.assigning.model.solver.TaskHelper.isPotentialOwner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestSolver extends BaseTaskAssigningTest {

    private static final long MILLISECONDS_TIME_SPENT_LIMIT = 10000;

    @Test
    public void startSolverAndSolution24Tasks8UsersTest() throws Exception {
        testSolverStartAndSolution(MILLISECONDS_TIME_SPENT_LIMIT, _24TASKS_8USERS_SOLUTION);
    }

    @Test
    public void startSolverAndSolution50Tasks5UsersTest() throws Exception {
        testSolverStartAndSolution(MILLISECONDS_TIME_SPENT_LIMIT, _50TASKS_5USERS_SOLUTION);
    }

    @Test
    public void startSolverAndSolution100Tasks5UsersTest() throws Exception {
        testSolverStartAndSolution(MILLISECONDS_TIME_SPENT_LIMIT, _100TASKS_5USERS_SOLUTION);
    }

    @Test
    public void startSolverAndSolution500Tasks5UsersTest() throws Exception {
        testSolverStartAndSolution(MILLISECONDS_TIME_SPENT_LIMIT * 2, _500TASKS_20USERS_SOLUTION);
    }

    /**
     * Tests that solver for the tasks assigning problem definition can be properly started, a solution can be produced,
     * and that some minimal constrains are met by de solution.
     */
    private void testSolverStartAndSolution(long millisecondsSpentLimit, String solutionResource) throws Exception {
        Solver<TaskAssigningSolution> solver = createNonDaemonSolver(millisecondsSpentLimit);
        TaskAssigningSolution solution = readTaskAssigningSolution(solutionResource);
        solution.getUserList().add(User.PLANNING_USER);
        TaskAssigningSolution result = solver.solve(solution);
        if (!result.getScore().isFeasible()) {
            fail(String.format("With current problem definition and time spent of %s milliseconds it's expected " +
                                       "that a feasible solution has been produced.", millisecondsSpentLimit));
        }
        assertConstraints(result);

        StringBuilder builder = new StringBuilder();
        printSolution(result, builder);

        System.out.println();
        System.out.println(builder.toString());
    }

    /**
     * Given a TaskAssigningSolution asserts the following constraints.
     * <p>
     * 1) All tasks are assigned to a user
     * 2) The assigned user for a task is a potentialOwner for the task or the PLANNING_USER
     * 3) All tasks are assigned.
     * @param solution a solution.
     */
    private void assertConstraints(TaskAssigningSolution solution) {
        int[] totalTasks = {0};
        solution.getUserList().forEach(user -> {
            List<Task> taskList = extractTaskList(user);
            totalTasks[0] += taskList.size();
            taskList.forEach(task -> assertAssignment(user, task, solution.getUserList()));
        });
        assertEquals(solution.getTaskList().size(), totalTasks[0]);
    }

    private void assertAssignment(User user, Task task, List<User> availableUsers) {
        assertNotNull(task.getUser());
        assertEquals("Task is not assigned to expected user", user.getEntityId(), task.getUser().getEntityId());
        if (task.getPotentialOwners() == null || task.getPotentialOwners().isEmpty()) {
            assertEquals("Task without potentialOwners can only be assigned to the PLANNING_USER", User.PLANNING_USER.getEntityId(), user.getEntityId());
        } else if (User.PLANNING_USER.getEntityId().equals(user.getEntityId())) {
            availableUsers.forEach(availableUser -> {
                assertFalse(String.format("PLANNING_USER user was assigned but another potential owner was found. user: %s task: %s", user, task), isPotentialOwner(task, user));
            });
        } else {
            assertTrue(String.format("User: %s is not a potential owner for task: %s", user, task), isPotentialOwner(task, user));
        }
    }
}