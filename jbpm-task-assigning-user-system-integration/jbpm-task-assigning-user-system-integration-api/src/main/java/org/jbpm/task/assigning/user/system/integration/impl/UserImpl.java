package org.jbpm.task.assigning.user.system.integration.impl;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.task.assigning.user.system.integration.Group;
import org.jbpm.task.assigning.user.system.integration.User;

public class UserImpl implements User {

    private String id;
    private Set<Group> groups;

    public UserImpl(String id) {
        this(id, new HashSet<>());
    }

    public UserImpl(String id, Set<Group> groups) {
        this.id = id;
        this.groups = groups;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<Group> getGroups() {
        return groups;
    }
}
