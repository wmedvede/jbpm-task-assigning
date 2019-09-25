package org.jbpm.services.task.assigning.model.solver.realtime;

import org.jbpm.services.task.assigning.model.Task;
import org.jbpm.services.task.assigning.model.TaskAssigningSolution;
import org.jbpm.services.task.assigning.model.TaskOrUser;
import org.jbpm.services.task.assigning.model.User;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

/**
 * Implements the removal of a Task.
 */
public class RemoveTaskProblemFactChange implements ProblemFactChange<TaskAssigningSolution> {

    private Task task;

    public RemoveTaskProblemFactChange(Task task) {
        this.task = task;
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
