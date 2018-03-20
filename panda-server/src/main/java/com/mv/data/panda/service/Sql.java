package com.mv.data.panda.service;

/**
 * author: houying
 * date  : 17-6-29
 * desc  :
 */
public interface Sql {

    String QUERY_CLUSTER_BY_DEP = "select b.master_address from yarn_clusters a join dataplatform.emr_cluster b "
            + "on a.cluster_name=b.cluster_name and a.dep=?";

    String QUERY_UN_FINISHED = "select id, cluster_master, application_id, state from submit where state in (0, 1, 2)";

    /** project 相关 sql START */
    String INSERT_PROJECT = "insert into project(name, dep, owner, managers, state, retry_count, description) value (?, ?, ?, ?, ?, ?, ?)";

    String UPDATE_PROJECT_ZIP_PATH = "update project set zip_path=?, state=if(state=0, 1, state) where id=?";

    String UPDATE_PROJECT_BY_ID = "update project set managers=?, retry_count=?, description=? where id=?";

    String UPDATE_PROJECT_STATE_BY_ID = "update project set state = ? where id = ?";

    String QUERY_PROJECT_BY_NAME = "select * from project where name=?";

    String QUERY_PROJECT_BY_ID = "select * from project where id=?";

    String QUERY_PROJECT_LIST = "select * from project where dep = ? order by id limit ?, ?";
    /** project 相关 sql END */

    /** instance 相关sql START **/
    String INSERT_INSTANCE = "insert into "
            + "submit(pid, submit_user, cluster_master, attempt, executor_num, executor_cores, executor_memory) "
            + "value (?, ?, ?, ?, ?, ?, ?)";

    String UPDATE_INSTANCE_STATE = "update submit set state = ? where id = ?";

    String UPDATE_SUBMIT_END   = "update submit set state=?, end_time=? where id=?";

    String UPDATE_INSTANCE = "update submit set application_id = ?, tracking_url = ?, start_time = ?, state = ? where id = ?";

    String QUERY_INSTANCE_BY_ID = "select * from submit where id=?";

    String QUERY_INSTANCE_BY_PID = "select * from submit where pid = ? order by id desc limit 1";
    /** instance 相关sql END **/

    /** history **/
    String QUERY_HISTORY_SUBMIT_BY_PID = "select * from submit where pid = ? and state in (3, 4, 5) order by id desc limit ?, ?";
}
