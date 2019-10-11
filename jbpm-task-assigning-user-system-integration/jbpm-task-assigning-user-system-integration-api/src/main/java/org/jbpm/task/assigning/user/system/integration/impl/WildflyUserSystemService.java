package org.jbpm.task.assigning.user.system.integration.impl;

import java.util.List;

import org.jbpm.task.assigning.user.system.integration.User;
import org.jbpm.task.assigning.user.system.integration.UserSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO move/refactor this class to the proper module/place, etc.
 * By now we just want an easy way of reading the users/groups from a WF configuration.
 */
public class WildflyUserSystemService implements UserSystemService {

    private static final String WF_ROLES_FILE = "/roles.properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(WildflyUserSystemService.class);

    WildflyUtil.UserGroupInfo userGroupInfo;

    public WildflyUserSystemService() {
        try {
            userGroupInfo = WildflyUtil.buildWildflyUsers(getClass(), WF_ROLES_FILE);
        } catch (Exception e) {
            LOGGER.error("An error was produced during users file loading", e);
        }
    }

    @Override
    public List<User> findAllUsers() {
        return userGroupInfo.getUsers();
    }
}
