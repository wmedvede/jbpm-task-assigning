package org.jbpm.services.task.assigning.solver.realtime;

import org.jbpm.services.task.assigning.model.TaskAssigningSolution;
import org.jbpm.services.task.assigning.model.solver.realtime.RemoveTaskProblemFactChange;

public class RemoveTaskProblemFactChangeTest extends BaseProblemFactChangeTest {

    public void removeTaskProblemFactChange50Tasks50UsersTest() throws Exception {
        removeTaskProblemFactChangeTest(_50TASKS_5USERS_SOLUTION);
    }

    private void removeTaskProblemFactChangeTest(String solutionResource) throws Exception {
        TaskAssigningSolution solution = readTaskAssigningSolution(solutionResource);


    }
}
