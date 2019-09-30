package org.jbpm.task.assigning.process.runtime.integration.client;

import java.util.List;

public interface ProcessRuntimeIntegrationClient {

    List<TaskInfo> findTasksByStatus(List<String> status,
                                     Integer page,
                                     Integer pageSize,
                                     String sort,
                                     boolean sortOrder);

    List<TaskPlanningResult> planTasks(List<TaskPlanningInfo> planning);

}
