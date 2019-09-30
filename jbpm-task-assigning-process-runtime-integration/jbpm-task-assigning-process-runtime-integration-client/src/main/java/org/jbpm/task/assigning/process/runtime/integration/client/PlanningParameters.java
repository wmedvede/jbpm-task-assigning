package org.jbpm.task.assigning.process.runtime.integration.client;

import java.util.Objects;

/**
 * Keeps the information configured/assigned by OptaPlanner when a solution is planned into the jBPM runtime.
 * This information is good enough for restoring a solution and start the solver from the last planned solution.
 */
public class PlanningParameters {

    private String assignedUser;
    private int index;
    private boolean pinned;
    private boolean published;

    public PlanningParameters() {
    }

    public PlanningParameters(String assignedUser, int index, boolean pinned, boolean published) {
        this.assignedUser = assignedUser;
        this.index = index;
        this.pinned = pinned;
        this.published = published;
    }

    public String getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlanningParameters)) {
            return false;
        }
        PlanningParameters that = (PlanningParameters) o;
        return index == that.index &&
                pinned == that.pinned &&
                published == that.published &&
                Objects.equals(assignedUser, that.assignedUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignedUser, index, pinned, published);
    }
}
