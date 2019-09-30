package org.jbpm.task.assigning.process.runtime.integration.client;

/**
 * Keeps the information for executing a planning into the jBPM engine.
 */
public class TaskPlanningInfo {

    private String containerId;
    private long taskId;
    private long processInstanceId;
    private PlanningParameters planningParameters = new PlanningParameters();

    public TaskPlanningInfo() {
    }

    public TaskPlanningInfo(String containerId, long taskId, long processInstanceId) {
        this.containerId = containerId;
        this.taskId = taskId;
        this.processInstanceId = processInstanceId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public PlanningParameters getPlanningParameters() {
        return planningParameters;
    }
}
