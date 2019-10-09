package org.jbpm.task.assigning.runtime.service;

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
