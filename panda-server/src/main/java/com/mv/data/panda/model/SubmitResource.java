package com.mv.data.panda.model;

/**
 * author: houying
 * date  : 18-1-11
 * desc  :
 */
public class SubmitResource {
    int executorNum;
    int executorCores;
    String executorMemory;

    public SubmitResource(int executorNum, int executorCores, String executorMemory) {
        this.executorNum = executorNum;
        this.executorCores = executorCores;
        this.executorMemory = executorMemory;
    }

    public int getExecutorNum() {
        return executorNum;
    }

    public int getExecutorCores() {
        return executorCores;
    }

    public String getExecutorMemory() {
        return executorMemory;
    }
}
