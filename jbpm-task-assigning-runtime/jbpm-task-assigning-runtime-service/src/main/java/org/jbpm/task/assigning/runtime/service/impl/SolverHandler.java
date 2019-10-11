package org.jbpm.task.assigning.runtime.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.model.User;
import org.jbpm.task.assigning.model.solver.realtime.AddTaskProblemFactChange;
import org.jbpm.task.assigning.runtime.service.SolverDef;
import org.jbpm.task.assigning.process.runtime.integration.client.ProcessRuntimeIntegrationClient;
import org.jbpm.task.assigning.process.runtime.integration.client.TaskInfo;
import org.jbpm.task.assigning.runtime.service.TaskAssigningService;
import org.jbpm.task.assigning.user.system.integration.UserSystemService;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.InProgress;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Ready;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Reserved;
import static org.jbpm.task.assigning.process.runtime.integration.client.TaskStatus.Suspended;

public class SolverHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(SolverHandler.class);

    enum STATUS {
        CREATED,
        INITIALIZED,
        STARTED,
        DESTROYED
    }

    private SolverDef solverDef;
    private Solver<TaskAssigningSolution> solver;
    private ProcessRuntimeIntegrationClient runtimeClient;
    private UserSystemService userSystemService;

    private STATUS status = STATUS.CREATED;

    private TaskAssigningSolution currentSolution;

    int times = 2;

    public SolverHandler(SolverDef solverDef, ProcessRuntimeIntegrationClient runtimeClient, UserSystemService userSystemService) {
        this.solverDef = solverDef;
        this.runtimeClient = runtimeClient;
        this.userSystemService = userSystemService;
    }

    public void init() {
        solver = createSolver(solverDef);
        solver.addEventListener(this::onBestSolutionChange);
        status = STATUS.INITIALIZED;
    }

    public void start() {
        TaskAssigningSolution solution = loadSolution();
        solution = new TaskAssigningSolution();
        solution.setTaskList(new ArrayList<>());
        solution.setUserList(new ArrayList<>());
        solution.getTaskList().add(new Task(-1, "blabla", 3));
        solution.getUserList().add(new User(-1, "user1"));
        solution.getUserList().add(User.PLANNING_USER);
        solver.solve(solution);
        status = STATUS.STARTED;
    }

    public void destroy() {
        this.status = STATUS.DESTROYED;
        solver.terminateEarly();
    }

    public void addProblemFactChanges(List<ProblemFactChange<TaskAssigningService>> changes) {

    }

    private TaskAssigningSolution loadSolution() {
        final List<TaskInfo> taskInfos = runtimeClient.findTasks(Arrays.asList(Ready, Reserved, InProgress, Suspended), 0, 10000);
        final List<org.jbpm.task.assigning.user.system.integration.User> externalUsers = userSystemService.findAllUsers();
        return new SolutionBuilder()
                .withTasks(taskInfos)
                .withUsers(externalUsers)
                .build();
    }

    protected void onBestSolutionChange(BestSolutionChangedEvent<TaskAssigningSolution> event) {
        System.out.println("New Best Solution: " + event.getNewBestSolution());
        if (times > 0) {
            solver.addProblemFactChange(new AddTaskProblemFactChange(new Task(times, "TaskTimes_" + times, 1 )));
            times--;
        } else {
            //solver.terminateEarly();
        }
    }

    private Solver<TaskAssigningSolution> createSolver(SolverDef solverDef) {
        SolverFactory<TaskAssigningSolution> solverFactory = SolverFactory.createFromXmlResource(solverDef.getSolverConfigFile());
        return solverFactory.buildSolver();
    }
}
