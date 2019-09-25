package org.jbpm.services.task.assigning.solver.realtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.jbpm.services.task.assigning.model.TaskAssigningSolution;
import org.jbpm.services.task.assigning.solver.BaseTaskAssigningTest;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BaseProblemFactChangeTest<C extends ProblemFactChange<TaskAssigningSolution>> extends BaseTaskAssigningTest {

    protected class ProgrammedProblemFactChange {

        private TaskAssigningSolution solutionAfterChange;

        private C change;

        public ProgrammedProblemFactChange() {
        }

        public ProgrammedProblemFactChange(C change) {
            this.change = change;
        }

        public TaskAssigningSolution getSolutionAfterChange() {
            return solutionAfterChange;
        }

        public void setSolutionAfterChange(TaskAssigningSolution solutionAfterChange) {
            this.solutionAfterChange = solutionAfterChange;
        }

        public C getChange() {
            return change;
        }

        public void setChange(C change) {
            this.change = change;
        }
    }

    protected void executeSequentialChanges(TaskAssigningSolution solution, List<? extends ProgrammedProblemFactChange> changes) {
        Solver<TaskAssigningSolution> solver = createDaemonSolver();

        //store the first solution that was produced by the solver for knowing how things looked like at the very
        //beginning before any change was produced.
        final TaskAssigningSolution[] initialSolution = {null};
        final boolean[] changesInProgress = {false};

        final Semaphore programNextChange = new Semaphore(0);
        final Semaphore allChangesWereProduced = new Semaphore(0);

        //prepare the list of changes to program
        List<ProgrammedProblemFactChange> programmedChanges = new ArrayList<>(changes);
        List<ProgrammedProblemFactChange> scheduledChanges = new ArrayList<>();

        int totalProgrammedChanges = programmedChanges.size();
        int[] pendingChanges = {programmedChanges.size()};

        solver.addEventListener(event -> {
            if (initialSolution[0] == null) {
                //store the first produced solution for knowing how things looked like at the very beginning.
                initialSolution[0] = event.getNewBestSolution();
                //let the problem fact changes start being produced.
                programNextChange.release();
            } else if (changesInProgress[0] && event.isEveryProblemFactChangeProcessed()) {
                changesInProgress[0] = false;
                ProgrammedProblemFactChange programmedChange = scheduledChanges.get(scheduledChanges.size() - 1);
                programmedChange.setSolutionAfterChange(event.getNewBestSolution());

                if (pendingChanges[0] > 0) {
                    //let the Programmed changes producer produce next change
                    programNextChange.release();
                } else {
                    solver.terminateEarly();
                    allChangesWereProduced.release();
                }
            }
        });

        //Programmed changes producer Thread.
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            boolean hasMoreChanges = true;
            while (hasMoreChanges) {
                try {
                    //wait until next problem fact change can be added to the solver.
                    //by construction the lock is only released when no problem fact change is in progress.
                    programNextChange.acquire();
                    ProgrammedProblemFactChange programmedChange = programmedChanges.remove(0);
                    hasMoreChanges = !programmedChanges.isEmpty();
                    pendingChanges[0] = programmedChanges.size();
                    scheduledChanges.add(programmedChange);
                    changesInProgress[0] = true;
                    solver.addProblemFactChange(programmedChange.getChange());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                //wait until the solver listener has processed all the changes.
                allChangesWereProduced.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        solver.solve(solution);

        assertTrue(programmedChanges.isEmpty());
        assertEquals(totalProgrammedChanges, scheduledChanges.size());
        assertEquals(pendingChanges[0], 0);
    }
}
