package org.jbpm.services.task.assigning.model.solver.realtime;

import org.jbpm.services.task.assigning.TaskAssigningRuntimeException;
import org.jbpm.services.task.assigning.model.Task;
import org.jbpm.services.task.assigning.model.TaskAssigningSolution;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

/**
 * Adds a Task to the working solution. If a task with the given identifier already exists an exception is thrown.
 */
public class AddTaskProblemFactChange implements ProblemFactChange<TaskAssigningSolution> {

    private Task task;

    public AddTaskProblemFactChange(Task task) {
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
            throw new TaskAssigningRuntimeException(String.format("A task with the given identifier id: %s already exists", task.getId()));
        }
        scoreDirector.beforeEntityAdded(task);
        // Planning entity lists are already cloned by the SolutionCloner, no need to clone.
        solution.getTaskList().add(task);
        scoreDirector.afterEntityAdded(task);
        scoreDirector.triggerVariableListeners();
    }
}
