package com.mv.data.panda.dao;

import com.mv.data.panda.common.mapper.RowWrapper;
import com.mv.data.panda.model.Project;
import com.mv.data.panda.service.Sql;
import com.mv.data.panda.util.Utils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * author: houying
 * date  : 18-2-5
 * desc  :
 */
@Repository
public class ProjectDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private RowWrapper<Project> projectRowMapper = new RowWrapper<>(Project.class);

    public Project queryById(long id) {
        return Utils.listToOne(jdbcTemplate.query(Sql.QUERY_PROJECT_BY_ID, projectRowMapper, id));
    }

    public List<Project> listByDep(String dep, int page, int size) {
        int start = (page - 1) * size;
        return jdbcTemplate.query(Sql.QUERY_PROJECT_LIST, projectRowMapper, dep, start, size);
    }

    public void updateState(long projectId, int value) {
        jdbcTemplate.update(Sql.UPDATE_PROJECT_STATE_BY_ID, value, projectId);
    }
}
