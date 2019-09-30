package org.jbpm.task.assigning.process.runtime.integration.client;

import java.util.Objects;

public class PotentialOwner {

    private String entityId;
    private boolean user;

    public PotentialOwner(boolean user, String entityId) {
        this.user = user;
        this.entityId = entityId;
    }

    public String getEntityId() {
        return entityId;
    }

    public boolean isUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PotentialOwner)) {
            return false;
        }
        PotentialOwner that = (PotentialOwner) o;
        return user == that.user &&
                Objects.equals(entityId, that.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, user);
    }
}
