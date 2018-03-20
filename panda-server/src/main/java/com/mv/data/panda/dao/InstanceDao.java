package com.mv.data.panda.dao;

import com.mv.data.panda.common.mapper.RowWrapper;
import com.mv.data.panda.model.Submit;
import com.mv.data.panda.model.SubmitState;
import com.mv.data.panda.service.Sql;
import com.mv.data.panda.util.Utils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 * author: houying
 * date  : 18-2-5
 * desc  :
 */
@Repository
public class InstanceDao {
    @Resource
    private JdbcTemplate jdbcTemplate;
    private RowWrapper<Submit> submitRowWrapper = new RowWrapper<>(Submit.class);

    public int insert(long projectId, String submitUser, String clusterMaster, int attempt,
            int execNum, int execCores, String execMem) {
        PreparedStatementCreator creator = con -> {
            PreparedStatement ps = con.prepareStatement(Sql.INSERT_INSTANCE, Statement.RETURN_GENERATED_KEYS);
            Object[] args = new Object[]{projectId, submitUser, clusterMaster, attempt, execNum, execCores, execMem};
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(creator, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(long tid, String applicationId, String trackingUrl, String startTime, int state) {
        jdbcTemplate.update(Sql.UPDATE_INSTANCE, applicationId, trackingUrl, startTime, state, tid);
    }

    public void updateState(long tid, int state) {
        jdbcTemplate.update(Sql.UPDATE_INSTANCE_STATE, state, tid);

    }

    public Submit queryById(long tid) {
        List<Submit> list = jdbcTemplate.query(Sql.QUERY_INSTANCE_BY_ID, submitRowWrapper, tid);
        return Utils.listToOne(list);
    }

    public Submit queryRunningByPid(long projectId) {
        List<Submit> list = jdbcTemplate.query(Sql.QUERY_INSTANCE_BY_PID, submitRowWrapper, projectId);
        return Utils.listToOne(list);
    }

    public List<Submit> queryHistoryByPid(long projectId, int start, int size) {
        return jdbcTemplate.query(Sql.QUERY_HISTORY_SUBMIT_BY_PID, submitRowWrapper, projectId, start, size);
    }

    public void updateEndTime(long id, int state, Date endTime) {
        jdbcTemplate.update(Sql.UPDATE_SUBMIT_END, state, endTime, id);
    }
}
