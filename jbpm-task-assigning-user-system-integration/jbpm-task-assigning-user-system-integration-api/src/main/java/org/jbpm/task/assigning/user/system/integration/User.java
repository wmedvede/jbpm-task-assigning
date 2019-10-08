package org.jbpm.task.assigning.user.system.integration;

import java.util.Set;

public interface User {

    String getId();

    Set<Group> getGroups();

}
