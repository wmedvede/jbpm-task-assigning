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

package org.jbpm.services.task.assigning.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

@PlanningEntity
public abstract class TaskOrUser extends AbstractPersistable {

    /**
     * Shadow variable for being able to move forward in the chain. So finally by using the nextTask and the
     * previousUserOrTask a double linked structure is created.
     *
     * <p>
     * User (the anchor) <-> A <-> B <-> C <-> D -> null
     * <p>
     * <p>
     * In this way given a TaskOrUser in a solution it's possible to iterate back and forward through the data structure.
     */
    @InverseRelationShadowVariable(sourceVariableName = "previousTaskOrUser")
    // Ignore this in the json result to keep Tasks details in taskList only
    @JsonIgnore
    protected Task nextTask;

    public TaskOrUser() {
    }

    public TaskOrUser(long id) {
        super(id);
    }

    public Task getNextTask() {
        return nextTask;
    }

    public void setNextTask(Task nextTask) {
        this.nextTask = nextTask;
    }

    /**
     * @return The endTime of a task in minutes. Can be null when the endTime of a just created task wasn't yet
     * calculated.
     */
    public abstract Integer getEndTime();

    /**
     * @return sometimes null, when a just created task wasn't yet assigned.
     */
    public abstract User getUser();
}
