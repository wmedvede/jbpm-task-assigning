package org.jbpm.services.task.assigning.solver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.jbpm.services.task.assigning.model.Group;
import org.jbpm.services.task.assigning.model.User;
import org.junit.Ignore;
import org.junit.Test;

public class WildflyUtil {

    private static final String WF_ROLES_FILE = "/data/roles.properties";

    @Ignore
    @Test
    public void testBuildWFUsers() throws Exception {
        UserGroupInfo info = buildWildflyUsers(WF_ROLES_FILE);
    }

    /**
     * Reads a Wildfly roles.properties configuration file and extracts the user definitions and the corresponding
     * groups. Can be useful when we start processing jBPM runtime tasks and we want to build the users configuration.
     * @param resource url for a resource in the classpath with the roles.properties file.
     * @return a UserGroupInfo instance with the Users and Groups loaded.
     * @throws IOException
     */
    UserGroupInfo buildWildflyUsers(String resource) throws IOException {
        final int[] userIds = {0};
        final int[] groupIds = {0};
        final List<User> users = new ArrayList<>();
        final List<Group> groups = new ArrayList<>();
        final Map<String, Group> groupMap = new HashMap<>();

        try (InputStream input = getClass().getResourceAsStream(resource)) {
            List<String> lines = IOUtils.readLines(input, StandardCharsets.UTF_8).stream()
                    .filter(line -> !line.startsWith("#"))
                    .collect(Collectors.toList());
            lines.forEach(line -> {
                String[] lineSplit = line.split("=");
                String userLogin = lineSplit[0];
                User user = new User(userIds[0]++, userLogin);
                users.add(user);
                Set<Group> userGroups = new HashSet<>();
                String encodedGroups = lineSplit[1];
                Stream.of(encodedGroups.split(",")).forEach(groupName -> {
                    Group group = groupMap.get(groupName);
                    if (group == null) {
                        group = new Group(groupIds[0]++, groupName);
                        groupMap.put(groupName, group);
                        groups.add(group);
                    }
                    userGroups.add(group);
                });
                user.setGroups(userGroups);
            });
        }
        return new UserGroupInfo(users, groups);
    }

    static class UserGroupInfo {

        List<User> users;
        List<Group> groups;

        UserGroupInfo(List<User> users, List<Group> groups) {
            this.users = users;
            this.groups = groups;
        }
    }
}
