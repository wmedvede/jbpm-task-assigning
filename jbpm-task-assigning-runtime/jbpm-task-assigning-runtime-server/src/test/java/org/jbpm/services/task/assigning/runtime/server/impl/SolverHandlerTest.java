package org.jbpm.services.task.assigning.runtime.server.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbpm.services.task.assigning.model.Task;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SolverHandlerTest {

    @Test
    public void addInOrderTest() {

        SolverHandler.AssignedTask task1 = new SolverHandler.AssignedTask(new Task(-1, "Task1", -1), 4, false, true);
        SolverHandler.AssignedTask task2 = new SolverHandler.AssignedTask(new Task(-1, "Task2", -1), 7, false, true);
        SolverHandler.AssignedTask task3 = new SolverHandler.AssignedTask(new Task(-1, "Task3", -1), 1, false, false);
        SolverHandler.AssignedTask task4 = new SolverHandler.AssignedTask(new Task(-1, "Task4", -1), 3, false, false);
        SolverHandler.AssignedTask task5 = new SolverHandler.AssignedTask(new Task(-1, "Task5", -1), 8, false, false);
        List<SolverHandler.AssignedTask> tasks = new ArrayList<>(Arrays.asList(task1, task2, task3, task4, task5));

        List<SolverHandler.AssignedTask> tasksByAdding = new ArrayList<>();
        tasks.forEach(assignedTask -> SolverHandler.addInOrder(tasksByAdding, assignedTask.getTask(), assignedTask.getIndex(), assignedTask.isPublished(), assignedTask.isPinned()));
        for (int i = 0; i < tasks.size(); i++) {
            assertEquals(tasks.get(i).getTask().getName(), tasksByAdding.get(i).getTask().getName());
        }

        Task newTask6 = new Task(-1, "newTask6", -1);
        Task newTask7 = new Task(-1, "newTask7", -1);
        Task newTask8 = new Task(-1, "newTask8", -1);
        Task newTask9 = new Task(-1, "newTask9", -1);
        Task newTask10 = new Task(-1, "newTask10", -1);
        Task newTask11 = new Task(-1, "newTask11", -1);
        Task newTask12 = new Task(-1, "newTask12", -1);
        Task newTask13 = new Task(-1, "newTask13", -1);
        Task newTask14 = new Task(-1, "newTask14", -1);

        SolverHandler.addInOrder(tasks, newTask6, -1, false, true);
        assertTaskInPosition(tasks, newTask6, 2);

        SolverHandler.addInOrder(tasks, newTask7, -1, false, true);
        assertTaskInPosition(tasks, newTask7, 3);

        SolverHandler.addInOrder(tasks, newTask8, 5, false, true);
        assertTaskInPosition(tasks, newTask8, 1);

        SolverHandler.addInOrder(tasks, newTask9, 0, false, false);
        assertTaskInPosition(tasks, newTask9, 5);

        SolverHandler.addInOrder(tasks, newTask10, -1, false, false);
        assertTaskInPosition(tasks, newTask10, 9);

        SolverHandler.addInOrder(tasks, newTask11, 2, false, false);
        assertTaskInPosition(tasks, newTask11, 7);

        SolverHandler.addInOrder(tasks, newTask12, 7, false, true);
        assertTaskInPosition(tasks, newTask12, 3);

        SolverHandler.addInOrder(tasks, newTask13, 2, false, false);
        assertTaskInPosition(tasks, newTask13, 9);

        SolverHandler.addInOrder(tasks, newTask14, 3, false, true);
        assertTaskInPosition(tasks, newTask14, 0);
    }

    private void assertTaskInPosition(List<SolverHandler.AssignedTask> tasks, Task expectedTask, int index) {
        assertEquals(expectedTask, tasks.get(index).getTask());
    }
}
