package com.mv.data.panda.model;


import com.mv.data.panda.common.mapper.Column;

import java.sql.Timestamp;
import java.util.Date;

/**
 * author: houying
 * date  : 17-6-29
 * desc  :
 */
public class Submit {
    @Column("id")
    private long id;

    @Column("pid")
    private long projectId;

    @Column("submit_user")
    private String submitUser;

    @Column("cluster_master")
    private String clusterMaster;

    private int attempt;

    @Column("executor_num")
    private int executorNum;

    @Column("executor_cores")
    private int executorCores;

    @Column("executor_memory")
    private String executorMemory;

    @Column("application_id")
    private String applicationId;

    @Column("tracking_url")
    private String trackingUrl;

    @Column("start_time")
    private Date startTime;

    @Column("end_time")
    private Date endTime;

    private int state;

    @Column("update_time")
    private Timestamp updateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getSubmitUser() {
        return submitUser;
    }

    public void setSubmitUser(String submitUser) {
        this.submitUser = submitUser;
    }

    public String getClusterMaster() {
        return clusterMaster;
    }

    public void setClusterMaster(String clusterMaster) {
        this.clusterMaster = clusterMaster;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public int getExecutorNum() {
        return executorNum;
    }

    public void setExecutorNum(int executorNum) {
        this.executorNum = executorNum;
    }

    public int getExecutorCores() {
        return executorCores;
    }

    public void setExecutorCores(int executorCores) {
        this.executorCores = executorCores;
    }

    public String getExecutorMemory() {
        return executorMemory;
    }

    public void setExecutorMemory(String executorMemory) {
        this.executorMemory = executorMemory;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
