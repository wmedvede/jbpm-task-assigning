package org.jbpm.task.assigning.model.solver.realtime;

import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.model.TaskOrUser;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

/**
 * Implements the removal of a Task from the working solution. If a task with the given identifier not exists it does
 * no action.
 */
public class RemoveTaskProblemFactChange implements ProblemFactChange<TaskAssigningSolution> {

    private Task task;

    public RemoveTaskProblemFactChange(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    @Override
    public void doChange(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        TaskAssigningSolution solution = scoreDirector.getWorkingSolution();
        Task workingTask = scoreDirector.lookUpWorkingObjectOrReturnNull(task);
        if (workingTask != null) {
            TaskOrUser previousTaskOrUser = workingTask.getPreviousTaskOrUser();
            Task nextTask = workingTask.getNextTask();
            if (nextTask != null) {
                scoreDirector.beforeVariableChanged(nextTask, "previousTaskOrUser");
                nextTask.setPreviousTaskOrUser(previousTaskOrUser);
                scoreDirector.afterVariableChanged(nextTask, "previousTaskOrUser");
            }
            scoreDirector.beforeEntityRemoved(workingTask);
            // Planning entity lists are already cloned by the SolutionCloner, no need to clone.
            solution.getTaskList().remove(workingTask);
            scoreDirector.afterEntityRemoved(workingTask);
            scoreDirector.triggerVariableListeners();
        }
    }
}
