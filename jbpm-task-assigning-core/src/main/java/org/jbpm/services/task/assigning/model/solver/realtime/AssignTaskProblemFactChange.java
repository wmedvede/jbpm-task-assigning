package org.jbpm.services.task.assigning.model.solver.realtime;

import org.jbpm.services.task.assigning.TaskAssigningInternalException;
import org.jbpm.services.task.assigning.model.Task;
import org.jbpm.services.task.assigning.model.TaskAssigningSolution;
import org.jbpm.services.task.assigning.model.TaskOrUser;
import org.jbpm.services.task.assigning.model.User;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

/**
 * Implements the "direct" assignment of an existing Task to a User.
 * This PFC can be useful scenarios were e.g. a system administrator manually assigns a Task to a given user from the
 * jBPM tasks list administration. While it's expected that environments that relied the tasks assigning to OptaPlanner
 * shouldn't do this "direct" assignments, we still provide this PFC for dealing with this edge case scenarios.
 * Note that this use cases might break hard constraints or introduce considerable score penalization for soft
 * constraints.
 * Additionally since the "direct" assignment comes from an "external" system it'll remain pinned.
 * <p>
 * Both the task and user to work with are looked up by using their corresponding id's. If the task is not found it'll
 * be created and added to the working solution, while if the user is not found and exception will be thrown.
 */
public class AssignTaskProblemFactChange implements ProblemFactChange<TaskAssigningSolution> {

    private Task task;
    private User user;

    public AssignTaskProblemFactChange(Task task, User user) {
        this.task = task;
        this.user = user;
    }

    public Task getTask() {
        return task;
    }

    public User getUser() {
        return user;
    }

    @Override
    public void doChange(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        TaskAssigningSolution solution = scoreDirector.getWorkingSolution();

        User workingUser = scoreDirector.lookUpWorkingObjectOrReturnNull(user);
        if (workingUser == null) {
            throw new TaskAssigningInternalException("Expected user: " + user + " was not found in current working solution");
        }

        Task workingTask = scoreDirector.lookUpWorkingObjectOrReturnNull(task);
        if (workingTask == null) {
            // The task will be created by this PFC.
            // Planning entity lists are already cloned by the SolutionCloner, no need to clone.
            scoreDirector.beforeEntityAdded(task);
            solution.getTaskList().add(task);
            scoreDirector.afterEntityAdded(task);
            scoreDirector.triggerVariableListeners();
            workingTask = scoreDirector.lookUpWorkingObjectOrReturnNull(task);
        } else {
            //un-link the task from his previous chain/position.
            TaskOrUser previousTaskOrUser = workingTask.getPreviousTaskOrUser();
            Task nextTask = workingTask.getNextTask();
            if (nextTask != null) {
                //re-link the chain where the workingTask belonged if any
                scoreDirector.beforeVariableChanged(nextTask, "previousTaskOrUser");
                nextTask.setPreviousTaskOrUser(previousTaskOrUser);
                scoreDirector.afterVariableChanged(nextTask, "previousTaskOrUser");
            }
        }

        TaskOrUser insertPosition = findInsertPosition(workingUser);
        Task insertPositionNextTask = insertPosition.getNextTask();

        scoreDirector.beforeVariableChanged(workingTask, "previousTaskOrUser");
        workingTask.setPreviousTaskOrUser(insertPosition);
        scoreDirector.afterVariableChanged(workingTask, "previousTaskOrUser");

        if (insertPositionNextTask != null) {
            scoreDirector.beforeVariableChanged(insertPositionNextTask, "previousTaskOrUser");
            insertPositionNextTask.setPreviousTaskOrUser(workingTask);
            scoreDirector.afterVariableChanged(insertPositionNextTask, "previousTaskOrUser");
        }

        //TODO review at which moment do we have to set and invoke the pinned for this case..
        scoreDirector.beforeProblemPropertyChanged(workingTask);
        workingTask.setPinned(true);
        scoreDirector.afterProblemPropertyChanged(workingTask);

        scoreDirector.triggerVariableListeners();
    }

    /**
     * Find the first available "position" where a task can be added in the tasks chain for a given user.
     * <p>
     * For a chain like:
     * <p>
     * U -> T1 -> T2 -> T3 -> T4 -> null
     * <p>
     * if e.g. T3 is returned, a new task Tn will be later added in the following position.
     * <p>
     * U -> T1 -> T2 -> T3 -> Tn -> T4 -> null
     * @param user the for adding a task to.
     * @return the proper TaskOrUser object were a task can be added. This method will never return null.
     */
    private TaskOrUser findInsertPosition(User user) {
        TaskOrUser result = user;
        Task nextTask = user.getNextTask();
        while (nextTask != null && nextTask.isPinned()) {
            result = nextTask;
            nextTask = nextTask.getNextTask();
        }
        return result;
    }
}
