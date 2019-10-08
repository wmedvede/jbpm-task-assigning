package org.jbpm.task.assigning.user.system.integration.impl;

import org.jbpm.task.assigning.user.system.integration.Group;

public class GroupImpl implements Group {

    private String id;

    public GroupImpl(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
