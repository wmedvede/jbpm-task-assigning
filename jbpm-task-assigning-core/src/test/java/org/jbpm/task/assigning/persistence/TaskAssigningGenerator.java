/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.task.assigning.persistence;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jbpm.task.assigning.model.Group;
import org.jbpm.task.assigning.model.OrganizationalEntity;
import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.model.User;
import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.app.LoggingMain;
import org.optaplanner.examples.common.persistence.AbstractSolutionImporter;
import org.optaplanner.examples.common.persistence.generator.StringDataGenerator;

import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

import static org.optaplanner.examples.common.app.CommonApp.DATA_DIR_SYSTEM_PROPERTY;

/**
 * Helper class for generating a set of example solution files. Was used for generating the files below and doesn't need
 * to be executed unless this files needs to be re-generated.
 * <p>
 * test/resources/data/unsolved/24tasks-8users.xml
 * test/resources/data/unsolved/50tasks-5users.xml
 * test/resources/data/unsolved/100tasks-5users.xml
 * test/resources/data/unsolved/500tasks-20users.xml
 */
public class TaskAssigningGenerator extends LoggingMain {

    private static final int TASK_MAX_PRIORITY = 10;
    private static final int TASK_POTENTIAL_OWNERS_USER_SIZE_MINIMUM = 0;
    private static final int TASK_POTENTIAL_OWNERS_USER_SIZE_MAXIMUM = 2;
    private static final int TASK_POTENTIAL_OWNERS_GROUP_SIZE_MINIMUM = 0;
    private static final int TASK_POTENTIAL_OWNERS_GROUP_SIZE_MAXIMUM = 3;

    private static final int USER_GROUP_SIZE = 10;
    private static final int USER_GROUP_SET_SIZE_MINIMUM = 0;
    private static final int USER_GROUP_SET_MAXIMUM = 3;

    public static void main(String[] args) {
        TaskAssigningGenerator generator = new TaskAssigningGenerator();
        generator.writeTaskAssigningSolution(24, 8);
        generator.writeTaskAssigningSolution(50, 5);
        generator.writeTaskAssigningSolution(100, 5);
        generator.writeTaskAssigningSolution(500, 20);
    }

    private static final StringDataGenerator groupNameGenerator = new StringDataGenerator()
            .addPart(true, 0,
                     "HR",
                     "IT",
                     "PM",
                     "Sales",
                     "Legal",
                     "Marketing",
                     "Manager",
                     "Developer",
                     "Accounting",
                     "Support");

    private static final StringDataGenerator userNameGenerator = StringDataGenerator.buildFullNames();

    private final SolutionFileIO<TaskAssigningSolution> solutionFileIO;
    private final File outputDir;

    private Random random;

    private TaskAssigningGenerator() {
        System.setProperty(DATA_DIR_SYSTEM_PROPERTY, "jbpm-task-assigning-core/src/test/resources");
        solutionFileIO = new XStreamSolutionFileIO<>(TaskAssigningSolution.class);
        outputDir = new File(CommonApp.determineDataDir("data"), "unsolved");
    }

    private void writeTaskAssigningSolution(int taskListSize, int userListSize) {
        String fileName = determineFileName(taskListSize, userListSize);
        File outputFile = new File(outputDir, fileName + ".xml");
        TaskAssigningSolution solution = createTaskAssigningSolution(fileName, taskListSize, USER_GROUP_SIZE, userListSize);
        solutionFileIO.write(solution, outputFile);
        logger.info("Saved: {}", outputFile);
    }

    private String determineFileName(int taskListSize, int userListSize) {
        return taskListSize + "tasks-" + userListSize + "users";
    }

    private TaskAssigningSolution createTaskAssigningSolution(String fileName, int taskListSize, int groupListSize, int userListSize) {
        random = new Random(37);
        TaskAssigningSolution solution = new TaskAssigningSolution();
        solution.setId(0L);

        List<Group> groupList = createGroupList(groupListSize);
        createUserList(solution, userListSize, groupList);
        createTaskList(solution, taskListSize, groupList);

        BigInteger a = AbstractSolutionImporter.factorial(taskListSize + userListSize - 1);
        BigInteger b = AbstractSolutionImporter.factorial(userListSize - 1);
        BigInteger possibleSolutionSize = (a == null || b == null) ? null : a.divide(b);
        logger.info("TaskAssigningSolution {} has {} tasks, {} groups, and {} users with a search space of {}.",
                    fileName,
                    taskListSize,
                    groupListSize,
                    userListSize,
                    AbstractSolutionImporter.getFlooredPossibleSolutionSize(possibleSolutionSize));
        return solution;
    }

    private List<Group> createGroupList(int groupListSize) {
        List<Group> groupList = new ArrayList<>(groupListSize);
        groupNameGenerator.predictMaximumSizeAndReset(groupListSize);
        for (int i = 0; i < groupListSize; i++) {
            Group group = new Group();
            group.setId((long) i);
            String groupName = groupNameGenerator.generateNextValue();
            group.setEntityId(groupName);
            logger.trace("Created Group with entityId: ({}).", groupName);
            groupList.add(group);
        }
        return groupList;
    }

    private void createUserList(TaskAssigningSolution solution, int userListSize, List<Group> groupList) {
        List<User> userList = new ArrayList<>(userListSize);
        userNameGenerator.predictMaximumSizeAndReset(userListSize);
        for (int i = 0; i < userListSize; i++) {
            User user = new User();
            user.setId((long) i);
            String userName = userNameGenerator.generateNextValue();
            user.setEntityId(userName);
            int groupListSize = USER_GROUP_SET_SIZE_MINIMUM + random.nextInt(USER_GROUP_SET_MAXIMUM - USER_GROUP_SET_SIZE_MINIMUM);
            if (groupListSize > groupList.size()) {
                groupListSize = groupList.size();
            }
            Set<Group> groupSet = new LinkedHashSet<>(groupListSize);
            int groupListIndex = random.nextInt(groupList.size());
            for (int j = 0; j < groupListSize; j++) {
                groupSet.add(groupList.get(groupListIndex));
                groupListIndex = (groupListIndex + 1) % groupList.size();
            }
            user.setGroups(groupSet);
            logger.trace("Created user with entityId ({}).", userName);
            userList.add(user);
        }
        solution.setUserList(userList);
    }

    private void createTaskList(TaskAssigningSolution solution, int taskListSize, List<Group> groupList) {
        List<User> userList = solution.getUserList();
        List<Task> taskList = new ArrayList<>(taskListSize);

        for (int i = 0; i < taskListSize; i++) {
            Task task = new Task();
            task.setId((long) i);
            task.setName("Task_" + task.getId());
            task.setPriority(random.nextInt(TASK_MAX_PRIORITY + 1));

            Set<OrganizationalEntity> potentialOwners = new LinkedHashSet<>();

            int groupListIndex = random.nextInt(groupList.size());
            int groupPotentialOwnersSize = TASK_POTENTIAL_OWNERS_GROUP_SIZE_MINIMUM + random.nextInt(TASK_POTENTIAL_OWNERS_GROUP_SIZE_MAXIMUM - TASK_POTENTIAL_OWNERS_GROUP_SIZE_MINIMUM);
            if (groupPotentialOwnersSize > groupList.size()) {
                groupPotentialOwnersSize = groupList.size();
            }
            for (int j = 0; j < groupPotentialOwnersSize; j++) {
                potentialOwners.add(groupList.get(groupListIndex));
                groupListIndex = (groupListIndex + 1) % groupList.size();
            }

            int userListIndex = random.nextInt(userList.size());
            int userPotentialOwnersSize = TASK_POTENTIAL_OWNERS_USER_SIZE_MINIMUM + random.nextInt(TASK_POTENTIAL_OWNERS_USER_SIZE_MAXIMUM - TASK_POTENTIAL_OWNERS_USER_SIZE_MINIMUM);
            if (userPotentialOwnersSize > userList.size()) {
                userPotentialOwnersSize = userList.size();
            }
            for (int j = 0; j < userPotentialOwnersSize; j++) {
                potentialOwners.add(userList.get(userListIndex));
                userListIndex = (userListIndex + 1) % userList.size();
            }

            task.setPotentialOwners(potentialOwners);
            taskList.add(task);
        }
        solution.setTaskList(taskList);
    }
}
