package org.jbpm.task.assigning.user.system.integration.impl;

import java.util.Set;

import org.jbpm.task.assigning.user.system.integration.Group;

public class UserImpl implements Group {

    private String id;
    private Set<Group> groups;

    public UserImpl(String id, Set<Group> groups) {
        this.id = id;
        this.groups = groups;
    }

    public String getId() {
        return id;
    }

    public Set<Group> getGroups() {
        return groups;
    }
}
