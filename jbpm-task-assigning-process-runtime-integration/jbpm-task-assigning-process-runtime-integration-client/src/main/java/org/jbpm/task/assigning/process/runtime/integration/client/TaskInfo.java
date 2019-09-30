package org.jbpm.task.assigning.process.runtime.integration.client;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskInfo {

    private long taskId;
    LocalDateTime createdOn;
    long processInstanceId;
    String processId;
    String deploymentId;
    /*
            "status": "Reserved"
            "priority": "5",
            "name": "TheTaskName",
            "lastModificationDate": "2019-08-14 12:57:25.313",
            "actualOwner": "john",
            "potentialOwners": {
        "users" :  [ "john", "mary" ],
        "groups" : [" HR", "IT" ]
    },
            "slaStatus": "Violated",
            "slaDueDate" "2019-08-14 12:57:25.313",

            "taskInputs": {
        "skills": "sk1, sk2, sk3",     //values configured in process designer
                "affinities": "sk1, sk2, sk3", //values configured in process designer
                "planning-order": "3"          //last assigned order by the planning solution
        "planning-owner": "john"       //last assgined order by the planning solution
        "pinned" : false
*/

    }
