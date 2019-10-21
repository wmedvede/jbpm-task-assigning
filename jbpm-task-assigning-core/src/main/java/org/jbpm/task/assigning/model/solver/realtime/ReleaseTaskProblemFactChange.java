package org.jbpm.task.assigning.model.solver.realtime;

import jdk.nashorn.internal.ir.RuntimeNode;
import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

public class ReleaseTaskProblemFactChange implements ProblemFactChange<TaskAssigningSolution> {

    private long id;

    public ReleaseTaskProblemFactChange(long id) {
        this.id = id;
    }

    @Override
    public void doChange(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        //TODO implement here.
        throw new RuntimeException("fact change not implemented");
    }
}
