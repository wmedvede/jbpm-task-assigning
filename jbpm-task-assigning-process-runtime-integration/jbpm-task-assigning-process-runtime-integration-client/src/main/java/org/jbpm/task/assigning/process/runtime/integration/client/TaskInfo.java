package org.jbpm.task.assigning.process.runtime.integration.client;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class TaskInfo {

    private long taskId;
    private LocalDateTime createdOn;
    private long processInstanceId;
    private String processId;
    private String containerId;
    private TaskStatus status;
    private int priority;
    private String name;
    private LocalDateTime lastModificationDate;
    private String actualOwner;
    private Set<PotentialOwner> potentialOwners;
    private Map<String, Object> inputData;
    private PlanningParameters planningParameters;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(LocalDateTime lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public String getActualOwner() {
        return actualOwner;
    }

    public void setActualOwner(String actualOwner) {
        this.actualOwner = actualOwner;
    }

    public Set<PotentialOwner> getPotentialOwners() {
        return potentialOwners;
    }

    public void setPotentialOwners(Set<PotentialOwner> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }

    public Map<String, Object> getInputData() {
        return inputData;
    }

    public void setInputData(Map<String, Object> inputData) {
        this.inputData = inputData;
    }

    public PlanningParameters getPlanningParameters() {
        return planningParameters;
    }

    public void setPlanningParameters(PlanningParameters planningParameters) {
        this.planningParameters = planningParameters;
    }
}
