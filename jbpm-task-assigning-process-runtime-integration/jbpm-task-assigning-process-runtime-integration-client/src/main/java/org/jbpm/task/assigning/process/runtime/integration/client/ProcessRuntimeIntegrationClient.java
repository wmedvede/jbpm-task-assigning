package org.jbpm.task.assigning.process.runtime.integration.client;

import java.util.List;

public interface ProcessRuntimeIntegrationClient {

    List<TaskInfo> findTasks(List<TaskStatus> status,
                             Integer page,
                             Integer pageSize);

    List<TaskPlanningResult> applyPlanning(List<TaskPlanningInfo> planningInfos, String userId);
}
