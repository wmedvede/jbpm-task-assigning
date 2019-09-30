package org.jbpm.services.task.assigning.runtime.server;

public class SolverDef {

    private String solverId;
    private String solverConfigFile;

    public SolverDef(String solverId, String solverConfigFile) {
        this.solverId = solverId;
        this.solverConfigFile = solverConfigFile;
    }

    public String getSolverId() {
        return solverId;
    }

    public String getSolverConfigFile() {
        return solverConfigFile;
    }
}
